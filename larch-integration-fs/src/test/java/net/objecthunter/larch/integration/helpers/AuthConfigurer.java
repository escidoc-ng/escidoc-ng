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

package net.objecthunter.larch.integration.helpers;

import org.springframework.http.HttpMethod;

/**
 * @author mih
 */
public class AuthConfigurer {

    private final HttpMethod method;

    private final boolean html;

    private final String url;

    private final Object body;

    private final MissingPermission neededPermission;

    private final boolean resetState;

    private final String resetStateId;

    private final ObjectType resetStateObjectType;

    private final RoleRestriction roleRestriction;

    private AuthConfigurer(AuthConfigurerBuilder builder) {
        // private Constructor can only be called from AuthConfigurerBuilder
        this.method = builder.method;
        this.html = builder.html;
        this.url = builder.url;
        this.body = builder.body;
        this.neededPermission = builder.neededPermission;
        this.resetState = builder.resetState;
        this.resetStateId = builder.resetStateId;
        this.resetStateObjectType = builder.resetStateObjectType;
        this.roleRestriction = builder.roleRestriction;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public boolean isHtml() {
        return html;
    }

    public String getUrl() {
        return url;
    }

    public Object getBody() {
        return body;
    }

    public MissingPermission getNeededPermission() {
        return neededPermission;
    }

    public boolean isResetState() {
        return resetState;
    }

    public String getResetStateId() {
        return resetStateId;
    }

    public ObjectType getResetStateObjectType() {
        return resetStateObjectType;
    }

    public RoleRestriction getRoleRestriction() {
        return roleRestriction;
    }

    public static class AuthConfigurerBuilder {

        // mandatory parameter
        private final HttpMethod method;

        private final String url;

        // optional
        private boolean html = false;

        private Object body = null;

        private MissingPermission neededPermission = null;

        private boolean resetState = false;

        private String resetStateId = null;

        private ObjectType resetStateObjectType = ObjectType.ENTITY;

        private RoleRestriction roleRestriction = null;

        public AuthConfigurerBuilder(HttpMethod method, String url) {
            this.method = method;
            this.url = url;
        }

        public AuthConfigurerBuilder html(boolean html) {
            this.html = html;
            return this;
        }

        public AuthConfigurerBuilder body(Object body) {
            this.body = body;
            return this;
        }

        public AuthConfigurerBuilder neededPermission(MissingPermission neededPermission) {
            this.neededPermission = neededPermission;
            return this;
        }

        public AuthConfigurerBuilder resetState(boolean resetState) {
            this.resetState = resetState;
            return this;
        }

        public AuthConfigurerBuilder resetStateId(String resetStateId) {
            this.resetStateId = resetStateId;
            return this;
        }

        public AuthConfigurerBuilder resetStateObjectType(ObjectType resetStateObjectType) {
            this.resetStateObjectType = resetStateObjectType;
            return this;
        }

        public AuthConfigurerBuilder roleRestriction(RoleRestriction roleRestriction) {
            this.roleRestriction = roleRestriction;
            return this;
        }

        public AuthConfigurer build() {
            return new AuthConfigurer(this);
        }
    }

    public enum MissingPermission {
        READ_PENDING_METADATA,
        READ_SUBMITTED_METADATA,
        READ_PUBLISHED_METADATA,
        READ_WITHDRAWN_METADATA,
        WRITE_PENDING_METADATA,
        WRITE_SUBMITTED_METADATA,
        WRITE_PUBLISHED_METADATA,
        WRITE_WITHDRAWN_METADATA,
        READ_PENDING_BINARY,
        READ_SUBMITTED_BINARY,
        READ_PUBLISHED_BINARY,
        READ_WITHDRAWN_BINARY,
        WRITE_PENDING_BINARY,
        WRITE_SUBMITTED_BINARY,
        WRITE_PUBLISHED_BINARY,
        WRITE_WITHDRAWN_BINARY,
        READ_PERMISSION,
        WRITE_PERMISSION,
        ALL,
        NONE;
    }

    public enum RoleRestriction {
        ADMIN,
        USER,
        LOGGED_IN;
    }

    public enum ObjectType {
        WORKSPACE,
        ENTITY,
        USER,
        USER_REQUEST;
    }

}
