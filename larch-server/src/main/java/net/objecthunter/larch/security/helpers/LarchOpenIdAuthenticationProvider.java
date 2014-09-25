/*
 * Copyright 2014 Michael Hoppe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.larch.security.helpers;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.service.backend.BackendCredentialsService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.AuthenticationCancelledException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * @author mih
 */
public class LarchOpenIdAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(LarchOpenIdAuthenticationProvider.class);

    @Autowired
    private BackendCredentialsService backendCredentialsService;

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework.security
     * .Authentication)
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        if (!supports(authentication.getClass())) {
            return null;
        }

        if (authentication instanceof OpenIDAuthenticationToken) {
            OpenIDAuthenticationToken response = (OpenIDAuthenticationToken) authentication;
            OpenIDAuthenticationStatus status = response.getStatus();

            // handle the various possibilities
            if (status == OpenIDAuthenticationStatus.SUCCESS) {
                // Lookup user details
                User user = loadUserDetails(response);
                user = backendCredentialsService.addDefaultRights(user);

                return createSuccessfulAuthentication(user, response);

            } else if (status == OpenIDAuthenticationStatus.CANCELLED) {
                throw new AuthenticationCancelledException("Log in cancelled");
            } else if (status == OpenIDAuthenticationStatus.ERROR) {
                throw new AuthenticationServiceException("Error message from server: " + response.getMessage());
            } else if (status == OpenIDAuthenticationStatus.FAILURE) {
                throw new BadCredentialsException("Log in failed - identity could not be verified");
            } else if (status == OpenIDAuthenticationStatus.SETUP_NEEDED) {
                throw new AuthenticationServiceException(
                        "The server responded setup was needed, which shouldn't happen");
            } else {
                throw new AuthenticationServiceException("Unrecognized return value " + status.toString());
            }
        }

        return null;
    }

    /**
     * Handles the creation of the final <tt>Authentication</tt> object which will be returned by the provider.
     * <p>
     * The default implementation just creates a new OpenIDAuthenticationToken from the original, but with the
     * UserDetails as the principal and including the authorities loaded by the UserDetailsService.
     *
     * @param user the loaded User object
     * @param auth the token passed to the authenticate method, containing
     * @return the token which will represent the authenticated user.
     */
    protected Authentication createSuccessfulAuthentication(User user, OpenIDAuthenticationToken auth) {
        String[] roles = null;
        if (user != null) {
            if (user.getRoles() != null && user.getRoles().size() > 0) {
                roles = new String[user.getRoles().size()];
                int i = 0;
                for (Role role : user.getRoles()) {
                    roles[i] = role.getRoleName().name();
                    i++;
                }
            } else {
                roles = new String[] { "IDENTIFIED" };
            }
            return new OpenIDAuthenticationToken(user, AuthorityUtils.createAuthorityList(roles),
                    auth.getIdentityUrl(), auth.getAttributes());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.security.authentication.AuthenticationProvider#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return OpenIDAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Get user and roles from elasticsearch. initialize Spring-Security-User from it. If user does not exist in
     * elasticsearch, create new user without roles.
     * 
     * @param token OpenIDAuthenticationToken
     * @return User
     * @throws UsernameNotFoundException e
     */
    private User loadUserDetails(OpenIDAuthenticationToken token)
            throws UsernameNotFoundException {
        User user = null;
        // try to retrieve user.
        String encryptedUserName = encrypt(token.getName());
        try {
            user = backendCredentialsService.retrieveUser(encryptedUserName);
        } catch (IOException e) {
            log.info("user with name " + encryptedUserName + " not found");
        }

        // if user was not found, create user
        if (user == null) {
            user = createUser(token, encryptedUserName);
        }

        return user;
    }

    /**
     * Read attributes from token and create user with it. eMail-Address is mandatory, so throw exception if
     * eMail-Address not found in attributes.
     * 
     * @param token
     * @return larch-user-object
     * @throws UsernameNotFoundException
     */
    private net.objecthunter.larch.model.security.User createUser(OpenIDAuthenticationToken token,
            String encryptedUserName)
            throws UsernameNotFoundException {
        net.objecthunter.larch.model.security.User user = new net.objecthunter.larch.model.security.User();
        user.setName(encryptedUserName);
        // check for eMail-address
        boolean eMailAddressFound = false;
        if (token.getAttributes() != null) {
            for (OpenIDAttribute att : token.getAttributes()) {
                if (att.getType() != null &&
                        (OpenIdTypes.EMAIL.getNames().contains(att.getType()) ||
                                OpenIdTypes.FIRSTNAME.getNames().contains(att.getType()) ||
                                OpenIdTypes.LASTNAME.getNames().contains(att.getType()) || OpenIdTypes.FULLNAME
                                .getNames().contains(att.getType()))) {
                    if (att.getValues() != null && !att.getValues().isEmpty() && att.getValues().get(0) != null &&
                            StringUtils.isNotBlank(att.getValues().get(0))) {
                        if (OpenIdTypes.EMAIL.getNames().contains(att.getType())) {
                            user.setEmail(att.getValues().get(0));
                            eMailAddressFound = true;
                        }
                        else if (OpenIdTypes.FIRSTNAME.getNames().contains(att.getType())) {
                            user.setFirstName(att.getValues().get(0));
                        }
                        else if (OpenIdTypes.LASTNAME.getNames().contains(att.getType())) {
                            user.setLastName(att.getValues().get(0));
                        }
                        else if (OpenIdTypes.FULLNAME.getNames().contains(att.getType())) {
                            user.setLastName(att.getValues().get(0));
                        }
                    }
                }
            }
        }
        if (!eMailAddressFound) {
            throw new UsernameNotFoundException("User-Attributes did not contain a valid eMail-Address");
        }
        try {
            backendCredentialsService.createUser(user);
        } catch (IOException e) {
            throw new UsernameNotFoundException("Couldnt create user: " + e.getMessage());
        }
        return user;
    }

    /**
     * Encrypt String with MD5.
     * 
     * @param name
     * @return String encrypted String
     * @throws UsernameNotFoundException
     */
    private String encrypt(String name) throws UsernameNotFoundException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] nameBytes = name.getBytes();
            md.reset();
            byte[] digested = md.digest(nameBytes);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digested.length; i++) {
                sb.append(Integer.toHexString(0xff & digested[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new UsernameNotFoundException(ex.getMessage());
        }
    }

    /**
     * Enum holding possible openId-Attribute names.
     * 
     * @author mih
     */
    private enum OpenIdTypes {
        EMAIL(new ArrayList<String>() {

            {
                add("http://axschema.org/contact/email");
                add("http://schema.openid.net/contact/email");
            }
        }),
        FIRSTNAME(new ArrayList<String>() {

            {
                add("http://axschema.org/namePerson/first");
            }
        }),
        LASTNAME(new ArrayList<String>() {

            {
                add("http://axschema.org/namePerson/last");
            }
        }),
        FULLNAME(new ArrayList<String>() {

            {
                add("http://axschema.org/namePerson");
                add("http://schema.openid.net/namePerson");
            }
        });

        private final ArrayList<String> names;

        public ArrayList<String> getNames() {
            return names;
        }

        OpenIdTypes(ArrayList<String> names) {
            this.names = names;
        }

    }
}
