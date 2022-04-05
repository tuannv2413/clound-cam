--
-- Copyright © 2016-2021 The Thingsboard Authors
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE IF NOT EXISTS tb_schema_settings
(
    schema_version bigint NOT NULL,
    CONSTRAINT tb_schema_settings_pkey PRIMARY KEY (schema_version)
);

CREATE OR REPLACE PROCEDURE insert_tb_schema_settings()
    LANGUAGE plpgsql AS
$$
BEGIN
    IF (SELECT COUNT(*) FROM tb_schema_settings) = 0 THEN
        INSERT INTO tb_schema_settings (schema_version) VALUES (3003000);
    END IF;
END;
$$;

call insert_tb_schema_settings();

CREATE TABLE IF NOT EXISTS admin_settings
(
    id           uuid   NOT NULL
        CONSTRAINT admin_settings_pkey PRIMARY KEY,
    created_time bigint NOT NULL,
    json_value   varchar,
    key          varchar(255)
);

CREATE TABLE IF NOT EXISTS alarm
(
    id                       uuid   NOT NULL
        CONSTRAINT alarm_pkey PRIMARY KEY,
    created_time             bigint NOT NULL,
    ack_ts                   bigint,
    clear_ts                 bigint,
    additional_info          varchar,
    end_ts                   bigint,
    originator_id            uuid,
    originator_type          integer,
    propagate                boolean,
    severity                 varchar(255),
    start_ts                 bigint,
    status                   varchar(255),
    tenant_id                uuid,
    customer_id              uuid,
    propagate_relation_types varchar,
    type                     varchar(255)
);

CREATE TABLE IF NOT EXISTS asset
(
    id              uuid   NOT NULL
        CONSTRAINT asset_pkey PRIMARY KEY,
    created_time    bigint NOT NULL,
    additional_info varchar,
    customer_id     uuid,
    name            varchar(255),
    label           varchar(255),
    search_text     varchar(255),
    tenant_id       uuid,
    type            varchar(255),
    CONSTRAINT asset_name_unq_key UNIQUE (tenant_id, name)
);

CREATE TABLE IF NOT EXISTS audit_log
(
    id                     uuid   NOT NULL
        CONSTRAINT audit_log_pkey PRIMARY KEY,
    created_time           bigint NOT NULL,
    tenant_id              uuid,
    customer_id            uuid,
    entity_id              uuid,
    entity_type            varchar(255),
    entity_name            varchar(255),
    user_id                uuid,
    user_name              varchar(255),
    action_type            varchar(255),
    action_data            varchar(1000000),
    action_status          varchar(255),
    action_failure_details varchar(1000000)
);

CREATE TABLE IF NOT EXISTS attribute_kv
(
    entity_type    varchar(255),
    entity_id      uuid,
    attribute_type varchar(255), --SERVER_SCOPE
    attribute_key  varchar(255), --trạng thái = active
    bool_v         boolean,      --active T/F
    str_v          varchar(10000000),
    long_v         bigint,
    dbl_v          double precision,
    json_v         json,
    last_update_ts bigint,
    CONSTRAINT attribute_kv_pkey PRIMARY KEY (entity_type, entity_id, attribute_type, attribute_key)
);

CREATE TABLE IF NOT EXISTS component_descriptor
(
    id                       uuid   NOT NULL
        CONSTRAINT component_descriptor_pkey PRIMARY KEY,
    created_time             bigint NOT NULL,
    actions                  varchar(255),
    clazz                    varchar UNIQUE,
    configuration_descriptor varchar,
    name                     varchar(255),
    scope                    varchar(255),
    search_text              varchar(255),
    type                     varchar(255)
);

CREATE TABLE IF NOT EXISTS customer
(
    id              uuid   NOT NULL
        CONSTRAINT customer_pkey PRIMARY KEY,
    created_time    bigint NOT NULL,
    additional_info varchar,
    address         varchar,
    address2        varchar,
    city            varchar(255),
    country         varchar(255),
    email           varchar(255),
    phone           varchar(255),
    search_text     varchar(255),
    state           varchar(255),
    tenant_id       uuid,
    title           varchar(255),
    zip             varchar(255)
);

CREATE TABLE IF NOT EXISTS dashboard
(
    id                 uuid   NOT NULL
        CONSTRAINT dashboard_pkey PRIMARY KEY,
    created_time       bigint NOT NULL,
    configuration      varchar,
    assigned_customers varchar(1000000),
    search_text        varchar(255),
    tenant_id          uuid,
    title              varchar(255),
    mobile_hide        boolean DEFAULT false,
    mobile_order       int,
    image              varchar(1000000)
);

CREATE TABLE IF NOT EXISTS rule_chain
(
    id                 uuid   NOT NULL
        CONSTRAINT rule_chain_pkey PRIMARY KEY,
    created_time       bigint NOT NULL,
    additional_info    varchar,
    configuration      varchar(10000000),
    name               varchar(255),
    type               varchar(255),
    first_rule_node_id uuid,
    root               boolean,
    debug_mode         boolean,
    search_text        varchar(255),
    tenant_id          uuid
);

CREATE TABLE IF NOT EXISTS rule_node
(
    id              uuid   NOT NULL
        CONSTRAINT rule_node_pkey PRIMARY KEY,
    created_time    bigint NOT NULL,
    rule_chain_id   uuid,
    additional_info varchar,
    configuration   varchar(10000000),
    type            varchar(255),
    name            varchar(255),
    debug_mode      boolean,
    search_text     varchar(255)
);

CREATE TABLE IF NOT EXISTS rule_node_state
(
    id           uuid           NOT NULL
        CONSTRAINT rule_node_state_pkey PRIMARY KEY,
    created_time bigint         NOT NULL,
    rule_node_id uuid           NOT NULL,
    entity_type  varchar(32)    NOT NULL,
    entity_id    uuid           NOT NULL,
    state_data   varchar(16384) NOT NULL,
    CONSTRAINT rule_node_state_unq_key UNIQUE (rule_node_id, entity_id),
    CONSTRAINT fk_rule_node_state_node_id FOREIGN KEY (rule_node_id) REFERENCES rule_node (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ota_package
(
    id                 uuid         NOT NULL
        CONSTRAINT ota_package_pkey PRIMARY KEY,
    created_time       bigint       NOT NULL,
    tenant_id          uuid         NOT NULL,
    device_profile_id  uuid,
    type               varchar(32)  NOT NULL,
    title              varchar(255) NOT NULL,
    version            varchar(255) NOT NULL,
    tag                varchar(255),
    url                varchar(255),
    file_name          varchar(255),
    content_type       varchar(255),
    checksum_algorithm varchar(32),
    checksum           varchar(1020),
    data               oid,
    data_size          bigint,
    additional_info    varchar,
    search_text        varchar(255),
    CONSTRAINT ota_package_tenant_title_version_unq_key UNIQUE (tenant_id, title, version)
--     CONSTRAINT fk_device_profile_firmware FOREIGN KEY (device_profile_id) REFERENCES device_profile(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS device_profile
(
    id                    uuid   NOT NULL
        CONSTRAINT device_profile_pkey PRIMARY KEY,
    created_time          bigint NOT NULL,
    name                  varchar(255),
    type                  varchar(255),
    image                 varchar(1000000),
    transport_type        varchar(255),
    provision_type        varchar(255),
    profile_data          jsonb,
    description           varchar,
    search_text           varchar(255),
    is_default            boolean,
    tenant_id             uuid,
    firmware_id           uuid,
    software_id           uuid,
    default_rule_chain_id uuid,
    default_dashboard_id  uuid,
    default_queue_name    varchar(255),
    provision_device_key  varchar,
    CONSTRAINT device_profile_name_unq_key UNIQUE (tenant_id, name),
    CONSTRAINT device_provision_key_unq_key UNIQUE (provision_device_key),
    CONSTRAINT fk_default_rule_chain_device_profile FOREIGN KEY (default_rule_chain_id) REFERENCES rule_chain (id),
    CONSTRAINT fk_default_dashboard_device_profile FOREIGN KEY (default_dashboard_id) REFERENCES dashboard (id),
    CONSTRAINT fk_firmware_device_profile FOREIGN KEY (firmware_id) REFERENCES ota_package (id),
    CONSTRAINT fk_software_device_profile FOREIGN KEY (software_id) REFERENCES ota_package (id)
);

-- We will use one-to-many relation in the first release and extend this feature in case of user requests
-- CREATE TABLE IF NOT EXISTS device_profile_firmware (
--     device_profile_id uuid NOT NULL,
--     firmware_id uuid NOT NULL,
--     CONSTRAINT device_profile_firmware_unq_key UNIQUE (device_profile_id, firmware_id),
--     CONSTRAINT fk_device_profile_firmware_device_profile FOREIGN KEY (device_profile_id) REFERENCES device_profile(id) ON DELETE CASCADE,
--     CONSTRAINT fk_device_profile_firmware_firmware FOREIGN KEY (firmware_id) REFERENCES firmware(id) ON DELETE CASCADE,
-- );

CREATE TABLE IF NOT EXISTS device
(
    id                uuid   NOT NULL
        CONSTRAINT device_pkey PRIMARY KEY,
    created_time      bigint NOT NULL,
    additional_info   varchar,
    customer_id       uuid,
    device_profile_id uuid   NOT NULL,
    device_data       jsonb,
    type              varchar(255),
    name              varchar(255),
    label             varchar(255),
    search_text       varchar(255),
    tenant_id         uuid,
    firmware_id       uuid,
    software_id       uuid,
    CONSTRAINT device_name_unq_key UNIQUE (tenant_id, name),
    CONSTRAINT fk_device_profile FOREIGN KEY (device_profile_id) REFERENCES device_profile (id),
    CONSTRAINT fk_firmware_device FOREIGN KEY (firmware_id) REFERENCES ota_package (id),
    CONSTRAINT fk_software_device FOREIGN KEY (software_id) REFERENCES ota_package (id)
);

CREATE TABLE IF NOT EXISTS device_credentials
(
    id                uuid   NOT NULL
        CONSTRAINT device_credentials_pkey PRIMARY KEY,
    created_time      bigint NOT NULL,
    credentials_id    varchar,
    credentials_type  varchar(255),
    credentials_value varchar,
    device_id         uuid,
    CONSTRAINT device_credentials_id_unq_key UNIQUE (credentials_id),
    CONSTRAINT device_credentials_device_id_unq_key UNIQUE (device_id)
);

CREATE TABLE IF NOT EXISTS event
(
    id           uuid   NOT NULL
        CONSTRAINT event_pkey PRIMARY KEY,
    created_time bigint NOT NULL,
    body         varchar(10000000),
    entity_id    uuid,
    entity_type  varchar(255),
    event_type   varchar(255),
    event_uid    varchar(255),
    tenant_id    uuid,
    ts           bigint NOT NULL,
    CONSTRAINT event_unq_key UNIQUE (tenant_id, entity_type, entity_id, event_type, event_uid)
);

CREATE TABLE IF NOT EXISTS relation
(
    from_id             uuid,
    from_type           varchar(255),
    to_id               uuid,
    to_type             varchar(255),
    relation_type_group varchar(255),
    relation_type       varchar(255),
    additional_info     varchar,
    CONSTRAINT relation_pkey PRIMARY KEY (from_id, from_type, relation_type_group, relation_type, to_id, to_type)
);
-- ) PARTITION BY LIST (relation_type_group);
--
-- CREATE TABLE other_relations PARTITION OF relation DEFAULT;
-- CREATE TABLE common_relations PARTITION OF relation FOR VALUES IN ('COMMON');
-- CREATE TABLE alarm_relations PARTITION OF relation FOR VALUES IN ('ALARM');
-- CREATE TABLE dashboard_relations PARTITION OF relation FOR VALUES IN ('DASHBOARD');
-- CREATE TABLE rule_relations PARTITION OF relation FOR VALUES IN ('RULE_CHAIN', 'RULE_NODE');

CREATE TABLE IF NOT EXISTS tb_user
(
    id              uuid   NOT NULL
        CONSTRAINT tb_user_pkey PRIMARY KEY,
    created_time    bigint NOT NULL,
    additional_info varchar,
    authority       varchar(255),
    customer_id     uuid,
    email           varchar(255) UNIQUE,
    first_name      varchar(255),
    last_name       varchar(255),
    search_text     varchar(10485760),
    tenant_id       uuid
);

CREATE TABLE IF NOT EXISTS tenant_profile
(
    id                      uuid   NOT NULL
        CONSTRAINT tenant_profile_pkey PRIMARY KEY,
    created_time            bigint NOT NULL,
    name                    varchar(255),
    profile_data            jsonb,
    description             varchar,
    search_text             varchar(255),
    is_default              boolean,
    isolated_tb_core        boolean,
    isolated_tb_rule_engine boolean,
    CONSTRAINT tenant_profile_name_unq_key UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS tenant
(
    id                uuid   NOT NULL
        CONSTRAINT tenant_pkey PRIMARY KEY,
    created_time      bigint NOT NULL,
    additional_info   varchar,
    tenant_profile_id uuid   NOT NULL,
    address           varchar,
    address2          varchar,
    city              varchar(255),
    country           varchar(255),
    email             varchar(255),
    phone             varchar(255),
    region            varchar(255),
    search_text       varchar(255),
    state             varchar(255),
    title             varchar(255),
    zip               varchar(255),
    CONSTRAINT fk_tenant_profile FOREIGN KEY (tenant_profile_id) REFERENCES tenant_profile (id)
);

CREATE TABLE IF NOT EXISTS user_credentials
(
    id             uuid   NOT NULL
        CONSTRAINT user_credentials_pkey PRIMARY KEY,
    created_time   bigint NOT NULL,
    activate_token varchar(255) UNIQUE,
    enabled        boolean,
    password       varchar(255),
    reset_token    varchar(255) UNIQUE,
    user_id        uuid UNIQUE
);

CREATE TABLE IF NOT EXISTS widget_type
(
    id           uuid   NOT NULL
        CONSTRAINT widget_type_pkey PRIMARY KEY,
    created_time bigint NOT NULL,
    alias        varchar(255),
    bundle_alias varchar(255),
    descriptor   varchar(1000000),
    name         varchar(255),
    tenant_id    uuid,
    image        varchar(1000000),
    description  varchar(255)
);

CREATE TABLE IF NOT EXISTS widgets_bundle
(
    id           uuid   NOT NULL
        CONSTRAINT widgets_bundle_pkey PRIMARY KEY,
    created_time bigint NOT NULL,
    alias        varchar(255),
    search_text  varchar(255),
    tenant_id    uuid,
    title        varchar(255),
    image        varchar(1000000),
    description  varchar(255)
);

CREATE TABLE IF NOT EXISTS entity_view
(
    id              uuid   NOT NULL
        CONSTRAINT entity_view_pkey PRIMARY KEY,
    created_time    bigint NOT NULL,
    entity_id       uuid,
    entity_type     varchar(255),
    tenant_id       uuid,
    customer_id     uuid,
    type            varchar(255),
    name            varchar(255),
    keys            varchar(10000000),
    start_ts        bigint,
    end_ts          bigint,
    search_text     varchar(255),
    additional_info varchar
);

CREATE TABLE IF NOT EXISTS ts_kv_latest
(
    entity_id uuid   NOT NULL,
    key       int    NOT NULL,
    ts        bigint NOT NULL,
    bool_v    boolean,
    str_v     varchar(10000000),
    long_v    bigint,
    dbl_v     double precision,
    json_v    json,
    CONSTRAINT ts_kv_latest_pkey PRIMARY KEY (entity_id, key)
);

CREATE TABLE IF NOT EXISTS ts_kv_dictionary
(
    key    varchar(255) NOT NULL,
    key_id serial UNIQUE,
    CONSTRAINT ts_key_id_pkey PRIMARY KEY (key)
);

CREATE TABLE IF NOT EXISTS oauth2_params
(
    id           uuid   NOT NULL
        CONSTRAINT oauth2_params_pkey PRIMARY KEY,
    enabled      boolean,
    tenant_id    uuid,
    created_time bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS oauth2_registration
(
    id                             uuid   NOT NULL
        CONSTRAINT oauth2_registration_pkey PRIMARY KEY,
    oauth2_params_id               uuid   NOT NULL,
    created_time                   bigint NOT NULL,
    additional_info                varchar,
    client_id                      varchar(255),
    client_secret                  varchar(2048),
    authorization_uri              varchar(255),
    token_uri                      varchar(255),
    scope                          varchar(255),
    platforms                      varchar(255),
    user_info_uri                  varchar(255),
    user_name_attribute_name       varchar(255),
    jwk_set_uri                    varchar(255),
    client_authentication_method   varchar(255),
    login_button_label             varchar(255),
    login_button_icon              varchar(255),
    allow_user_creation            boolean,
    activate_user                  boolean,
    type                           varchar(31),
    basic_email_attribute_key      varchar(31),
    basic_first_name_attribute_key varchar(31),
    basic_last_name_attribute_key  varchar(31),
    basic_tenant_name_strategy     varchar(31),
    basic_tenant_name_pattern      varchar(255),
    basic_customer_name_pattern    varchar(255),
    basic_default_dashboard_name   varchar(255),
    basic_always_full_screen       boolean,
    custom_url                     varchar(255),
    custom_username                varchar(255),
    custom_password                varchar(255),
    custom_send_token              boolean,
    CONSTRAINT fk_registration_oauth2_params FOREIGN KEY (oauth2_params_id) REFERENCES oauth2_params (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS oauth2_domain
(
    id               uuid   NOT NULL
        CONSTRAINT oauth2_domain_pkey PRIMARY KEY,
    oauth2_params_id uuid   NOT NULL,
    created_time     bigint NOT NULL,
    domain_name      varchar(255),
    domain_scheme    varchar(31),
    CONSTRAINT fk_domain_oauth2_params FOREIGN KEY (oauth2_params_id) REFERENCES oauth2_params (id) ON DELETE CASCADE,
    CONSTRAINT oauth2_domain_unq_key UNIQUE (oauth2_params_id, domain_name, domain_scheme)
);

CREATE TABLE IF NOT EXISTS oauth2_mobile
(
    id               uuid   NOT NULL
        CONSTRAINT oauth2_mobile_pkey PRIMARY KEY,
    oauth2_params_id uuid   NOT NULL,
    created_time     bigint NOT NULL,
    pkg_name         varchar(255),
    app_secret       varchar(2048),
    CONSTRAINT fk_mobile_oauth2_params FOREIGN KEY (oauth2_params_id) REFERENCES oauth2_params (id) ON DELETE CASCADE,
    CONSTRAINT oauth2_mobile_unq_key UNIQUE (oauth2_params_id, pkg_name)
);

CREATE TABLE IF NOT EXISTS oauth2_client_registration_template
(
    id                             uuid   NOT NULL
        CONSTRAINT oauth2_client_registration_template_pkey PRIMARY KEY,
    created_time                   bigint NOT NULL,
    additional_info                varchar,
    provider_id                    varchar(255),
    authorization_uri              varchar(255),
    token_uri                      varchar(255),
    scope                          varchar(255),
    user_info_uri                  varchar(255),
    user_name_attribute_name       varchar(255),
    jwk_set_uri                    varchar(255),
    client_authentication_method   varchar(255),
    type                           varchar(31),
    basic_email_attribute_key      varchar(31),
    basic_first_name_attribute_key varchar(31),
    basic_last_name_attribute_key  varchar(31),
    basic_tenant_name_strategy     varchar(31),
    basic_tenant_name_pattern      varchar(255),
    basic_customer_name_pattern    varchar(255),
    basic_default_dashboard_name   varchar(255),
    basic_always_full_screen       boolean,
    comment                        varchar,
    login_button_icon              varchar(255),
    login_button_label             varchar(255),
    help_link                      varchar(255),
    CONSTRAINT oauth2_template_provider_id_unq_key UNIQUE (provider_id)
);

-- Deprecated
CREATE TABLE IF NOT EXISTS oauth2_client_registration_info
(
    id                             uuid   NOT NULL
        CONSTRAINT oauth2_client_registration_info_pkey PRIMARY KEY,
    enabled                        boolean,
    created_time                   bigint NOT NULL,
    additional_info                varchar,
    client_id                      varchar(255),
    client_secret                  varchar(255),
    authorization_uri              varchar(255),
    token_uri                      varchar(255),
    scope                          varchar(255),
    user_info_uri                  varchar(255),
    user_name_attribute_name       varchar(255),
    jwk_set_uri                    varchar(255),
    client_authentication_method   varchar(255),
    login_button_label             varchar(255),
    login_button_icon              varchar(255),
    allow_user_creation            boolean,
    activate_user                  boolean,
    type                           varchar(31),
    basic_email_attribute_key      varchar(31),
    basic_first_name_attribute_key varchar(31),
    basic_last_name_attribute_key  varchar(31),
    basic_tenant_name_strategy     varchar(31),
    basic_tenant_name_pattern      varchar(255),
    basic_customer_name_pattern    varchar(255),
    basic_default_dashboard_name   varchar(255),
    basic_always_full_screen       boolean,
    custom_url                     varchar(255),
    custom_username                varchar(255),
    custom_password                varchar(255),
    custom_send_token              boolean
);

-- Deprecated
CREATE TABLE IF NOT EXISTS oauth2_client_registration
(
    id                          uuid   NOT NULL
        CONSTRAINT oauth2_client_registration_pkey PRIMARY KEY,
    created_time                bigint NOT NULL,
    domain_name                 varchar(255),
    domain_scheme               varchar(31),
    client_registration_info_id uuid
);

CREATE TABLE IF NOT EXISTS api_usage_state
(
    id           uuid   NOT NULL
        CONSTRAINT usage_record_pkey PRIMARY KEY,
    created_time bigint NOT NULL,
    tenant_id    uuid,
    entity_type  varchar(32),
    entity_id    uuid,
    transport    varchar(32),
    db_storage   varchar(32),
    re_exec      varchar(32),
    js_exec      varchar(32),
    email_exec   varchar(32),
    sms_exec     varchar(32),
    alarm_exec   varchar(32),
    CONSTRAINT api_usage_state_unq_key UNIQUE (tenant_id, entity_id)
);

CREATE TABLE IF NOT EXISTS resource
(
    id            uuid         NOT NULL
        CONSTRAINT resource_pkey PRIMARY KEY,
    created_time  bigint       NOT NULL,
    tenant_id     uuid         NOT NULL,
    title         varchar(255) NOT NULL,
    resource_type varchar(32)  NOT NULL,
    resource_key  varchar(255) NOT NULL,
    search_text   varchar(255),
    file_name     varchar(255) NOT NULL,
    data          varchar,
    CONSTRAINT resource_unq_key UNIQUE (tenant_id, resource_type, resource_key)
);

CREATE TABLE IF NOT EXISTS edge
(
    id                 uuid   NOT NULL
        CONSTRAINT edge_pkey PRIMARY KEY,
    created_time       bigint NOT NULL,
    additional_info    varchar,
    customer_id        uuid,
    root_rule_chain_id uuid,
    type               varchar(255),
    name               varchar(255),
    label              varchar(255),
    routing_key        varchar(255),
    secret             varchar(255),
    edge_license_key   varchar(30),
    cloud_endpoint     varchar(255),
    search_text        varchar(255),
    tenant_id          uuid,
    CONSTRAINT edge_name_unq_key UNIQUE (tenant_id, name),
    CONSTRAINT edge_routing_key_unq_key UNIQUE (routing_key)
);

CREATE TABLE IF NOT EXISTS edge_event
(
    id                uuid   NOT NULL
        CONSTRAINT edge_event_pkey PRIMARY KEY,
    created_time      bigint NOT NULL,
    edge_id           uuid,
    edge_event_type   varchar(255),
    edge_event_uid    varchar(255),
    entity_id         uuid,
    edge_event_action varchar(255),
    body              varchar(10000000),
    tenant_id         uuid,
    ts                bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS rpc
(
    id              uuid              NOT NULL
        CONSTRAINT rpc_pkey PRIMARY KEY,
    created_time    bigint            NOT NULL,
    tenant_id       uuid              NOT NULL,
    device_id       uuid              NOT NULL,
    expiration_time bigint            NOT NULL,
    request         varchar(10000000) NOT NULL,
    response        varchar(10000000),
    additional_info varchar(10000000),
    status          varchar(255)      NOT NULL
);

-- Camera cloud database
CREATE TABLE IF NOT EXISTS cl_example
(
    id           uuid NOT NULL
        CONSTRAINT cl_example_pkey PRIMARY KEY,
    name         varchar(255),
    number       int8,
    checked      bool,
    created_time bigint,
    updated_time bigint,
    created_by   uuid,
    updated_by   uuid
);

-- start here

CREATE TABLE IF NOT EXISTS cl_user
(
    id           uuid         NOT NULL,
    tenant_id    uuid         NOT NULL,
    "name"       varchar(255) NOT NULL,
    phone        varchar(255),
    email        varchar(255),
    office       varchar(255),
    avatar       varchar(10485760),
    search_text  varchar(10485760),
    active       bool,
    type         varchar(255) NOT NULL, -- Loại tài khản
    delete       bool DEFAULT false,    --Xóa mềm tài khoản
    created_time bigint,
    created_by   uuid,
    updated_time bigint,
    updated_by   uuid,
    tb_user_id   uuid,
    CONSTRAINT cl_user_pkey PRIMARY KEY (id),
    CONSTRAINT fk_cluser_tbuser FOREIGN KEY (tb_user_id) REFERENCES tb_user (id)
);

CREATE TABLE IF NOT EXISTS cl_sysadmin
(
    id           uuid NOT NULL,
    address      varchar(4000),
    created_time bigint,
    created_by   uuid,
    updated_time bigint,
    updated_by   uuid,
    cl_user_id   uuid,
    CONSTRAINT cl_sysadmin_pkey PRIMARY KEY (id),
    CONSTRAINT fk_cluser_tbcustomer FOREIGN KEY (cl_user_id) REFERENCES cl_user (id)
);

CREATE TABLE IF NOT EXISTS cl_group_service
(
    id              uuid         NOT NULL,
    "name"          varchar(255) NOT NULL,
    max_day_storage int4         NOT NULL,
    note            varchar(4000),
    active          bool DEFAULT true,
    created_time    bigint,
    created_by      uuid,
    updated_time    bigint,
    updated_by      uuid,
    CONSTRAINT cl_group_service_pkey PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS cl_tenant
(
    id                uuid         NOT NULL,
    code              varchar(255) NOT NULL,
    type              varchar(255) NOT NULL,
    day_start_service bigint,
    address           varchar(4000),
    note              varchar(4000),
--     group_service_id  uuid, -- id của nhóm dịch vụ
--     service_option_id uuid, -- id option nhóm dịch vụ ứng với độ phân giải đã chọn
    created_time      bigint,
    created_by        uuid,
    updated_time      bigint,
    updated_by        uuid,
    cl_user_id        uuid,
    CONSTRAINT cl_tenant_pkey PRIMARY KEY (id),
    CONSTRAINT fk_cltenant_tbuser FOREIGN KEY (cl_user_id) REFERENCES cl_user (id)
--     CONSTRAINT fk_cltenant_clgroupservice FOREIGN KEY (group_service_id) REFERENCES cl_group_service (id)
);

CREATE TABLE IF NOT EXISTS cl_service_option
(
    id               uuid         NOT NULL,
    group_service_id uuid         NOT NULL,
    resolution       varchar(255) NOT NULL,
    price            int8         NOT NULL,
    CONSTRAINT cl_service_option_pkey PRIMARY KEY (id),
    CONSTRAINT fk_clgroup_service FOREIGN KEY (group_service_id) REFERENCES cl_group_service (id)
);

CREATE TABLE IF NOT EXISTS cl_service_option_and_cltenant
(
    cl_tenant_id      uuid not null,
    service_option_id uuid NOT NULL,
    CONSTRAINT cl_service_option_and_tenant_unq_key UNIQUE (cl_tenant_id, service_option_id),
    CONSTRAINT fk_clserviceoptioncltenant_cltenant FOREIGN KEY (cl_tenant_id) REFERENCES cl_tenant (id),
    CONSTRAINT fk_clserviceoptioncltenant_clserviceoptioncl FOREIGN KEY (service_option_id) REFERENCES cl_service_option (id)
);

CREATE TABLE IF NOT EXISTS cl_mange_media_box
(
    id                   uuid NOT NULL,
    tenant_id            uuid NOT NULL,
    origin_serial_number varchar(255),
    serial_number        varchar(255),
    part_number          varchar(255),
    consignment          varchar(255), -- Lô hàng
    type                 varchar(255), -- Loại box
    firmware_version     varchar(255), --Chú ý phải cập nhật với firmware thực tế trên thiết bị đã lắp đặt
    status               varchar(45),
    created_time         bigint,
    created_by           uuid,
    updated_time         bigint,
    updated_by           uuid,
    cl_tenant_id         uuid,
    CONSTRAINT cl_media_box_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cl_box
(
    id            uuid         NOT NULL,
    tenant_id     uuid         NOT NULL,
    box_name      varchar(255) NOT NULL,
    serial_number varchar(45)  NOT NULL,
    created_time  bigint,
    created_by    uuid,
    updated_time  bigint,
    updated_by    uuid,
    tb_device_id  uuid,
    media_box_id  uuid,
    is_delete     bool,
    CONSTRAINT cl_box_pkey PRIMARY KEY (id),
    CONSTRAINT fk_clbox_tbdevice FOREIGN KEY (tb_device_id) REFERENCES device (id),
    CONSTRAINT fk_clbox_clmediabox FOREIGN KEY (media_box_id) REFERENCES cl_mange_media_box (id)
);

CREATE TABLE IF NOT EXISTS cl_camera_group
(
    id                uuid         NOT NULL,
    tenant_id         uuid         NOT NULL,
    camera_group_name varchar(255) NOT NULL,
    is_default        bool DEFAULT false,
    index_setting     json         NOT NULL, -- list cameraId
    created_time      bigint,
    created_by        uuid,
    updated_time      bigint,
    updated_by        uuid,
    CONSTRAINT cl_camera_group_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cl_camera
(
    id              uuid         NOT NULL,
    tenant_id       uuid         NOT NULL,
    camera_name     varchar(255) NOT NULL,
--     "position"      varchar(4000),
    ipv4            varchar(45),
    camera_fisheye  bool,
    rtsp_username   varchar(255),
    rtsp_password   varchar(255),
    rtsp_port       int4,
    main_rtsp_url   varchar(4000),
    sub_rtsp_url    varchar(4000),
    onvif_username  varchar(255),
    onvif_password  varchar(255),
    onvif_port      int4,
    onvif_url       varchar(4000),
    rtmp_stream_id  varchar(255) NOT NULL,
    rtmp_url        varchar(400) NOT NULL,
    created_time    bigint,
    created_by      uuid,
    updated_time    bigint,
    updated_by      uuid,
    box_id          uuid,
    tb_device_id    uuid,
    camera_group_id uuid,
    is_delete       bool,
    CONSTRAINT cl_camera_pkey PRIMARY KEY (id),
    CONSTRAINT fk_clcamera_clbox FOREIGN KEY (box_id) REFERENCES cl_box (id),
    CONSTRAINT fk_clcamera_tbdevice FOREIGN KEY (tb_device_id) REFERENCES device (id),
    CONSTRAINT fk_clcamera_clcameragroup FOREIGN KEY (camera_group_id) REFERENCES cl_camera_group (id)
);

CREATE TABLE IF NOT EXISTS cl_customer_camera_permission
(
    id           uuid NOT NULL,
    cl_user_id   uuid NOT NULL,
    cl_camera_id uuid NOT NULL,
    live         bool,
    history      bool,
    ptz          bool,
    CONSTRAINT cl_customer_camera_permission_pkey PRIMARY KEY (id),
    CONSTRAINT customer_camera_permission_unq_key UNIQUE (cl_user_id, cl_camera_id),
    CONSTRAINT fk_clcustomercamerapermission_cluser FOREIGN KEY (cl_user_id) REFERENCES cl_user (id),
    CONSTRAINT fk_clcustomercamerapermission_clcamera FOREIGN KEY (cl_camera_id) REFERENCES cl_camera (id)
);

CREATE TABLE IF NOT EXISTS cl_role
(
    id           uuid          NOT NULL,
--     tenant_id    uuid          NOT NULL,
    role_name    varchar(255)  NOT NULL,
    role_type    varchar, --WEB_ADMIN: những role được tạo từ web admin | WEB_USER: những role cho web end user
    note         varchar(4000) NOT NULL,
    created_time bigint,
    created_by   uuid,
    updated_time bigint,
    updated_by   uuid,
    CONSTRAINT cl_role_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cl_permission
(
    id         uuid NOT NULL,
    permission varchar(255),
--     created_time bigint,
--     created_by   uuid,
--     updated_time bigint,
--     updated_by   uuid,
    CONSTRAINT cl_permission_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cl_role_and_permission
(
    cl_role_id       uuid NOT NULL,
    cl_permission_id uuid NOT NULL,
    CONSTRAINT cl_tbpermission_and_role_unq_key UNIQUE (cl_permission_id, cl_role_id),
    CONSTRAINT fk_clroleandpermission_clpermission FOREIGN KEY (cl_permission_id) REFERENCES cl_permission (id),
    CONSTRAINT fk_clroleandpermission_clrole FOREIGN KEY (cl_role_id) REFERENCES cl_role (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cl_tbuser_and_role
(
    tb_user_id uuid NOT NULL,
    cl_role_id uuid NOT NULL,
    CONSTRAINT cl_tbuser_and_role_unq_key UNIQUE (tb_user_id, cl_role_id),
    CONSTRAINT fk_cltbuserandrole_tbuser FOREIGN KEY (tb_user_id) REFERENCES tb_user (id),
    CONSTRAINT fk_cltbuserandrole_clrole FOREIGN KEY (cl_role_id) REFERENCES cl_role (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cl_camera_record_settting
(
    id             uuid         NOT NULL,
    tenant_id      uuid         NOT NULL,
    "name"         varchar(255) NOT NULL,
    type           varchar(255),
    setting        varchar      NOT NULL,
    active         bool,
    raw_setting_id uuid, -- id của record mà bản ghi mô phỏng
    created_time   bigint,
    created_by     uuid,
    updated_time   bigint,
    updated_by     uuid,
    camera_id      uuid,
    CONSTRAINT cl_camera_record_settting_pkey PRIMARY KEY (id),
    CONSTRAINT fk_clcamerasavedatasettting_clcamera FOREIGN KEY (camera_id) REFERENCES cl_camera (id)
);

CREATE TABLE IF NOT EXISTS cl_alarm
(
    id                 uuid         NOT NULL,
    tenant_id          uuid         NOT NULL,
    alarm_name         varchar(255) NOT NULL,
    "type"             varchar(45)  NOT NULL,
    via_notify         bool,
    via_sms            bool,
    via_email          bool,
    active             bool,
    time_alarm_setting varchar(4000),
    created_time       bigint,
    created_by         uuid,
    updated_time       bigint,
    updated_by         uuid,
    CONSTRAINT cl_alarm_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cl_tbdevice_and_clalarm
(
    tb_device_id uuid NOT NULL,
    cl_alarm_id  uuid NOT NULL,
    CONSTRAINT cl_tbdevice_and_clalarm_unq_key UNIQUE (tb_device_id, cl_alarm_id),
    CONSTRAINT fk_cltbdeviceandclalarm_tbdevice FOREIGN KEY (tb_device_id) REFERENCES device (id),
    CONSTRAINT fk_cltbdeviceandclalarm_clalarm FOREIGN KEY (cl_alarm_id) REFERENCES cl_alarm (id)
);

CREATE TABLE IF NOT EXISTS cl_alarm_revicers
(
    tb_user_id  uuid NOT NULL,
    cl_alarm_id uuid NOT NULL,
    CONSTRAINT cl_clalarmrevicers_and_clalarm_unq_key UNIQUE (tb_user_id, cl_alarm_id),
    CONSTRAINT fk_clalarmrevicers_tbuser FOREIGN KEY (tb_user_id) REFERENCES tb_user (id),
    CONSTRAINT fk_clalarmrevicers_clalarm FOREIGN KEY (cl_alarm_id) REFERENCES cl_alarm (id)
);

CREATE TABLE IF NOT EXISTS cl_user_mobile_notify_token
(
    id                  uuid NOT NULL,
    tb_user_id          uuid NOT NULL,
    mobile_notify_token varchar(4000) NOT NULL,
    created_time        bigint,
    created_by          uuid,
    CONSTRAINT cl_user_mobile_notify_token_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cl_alarm_history
(
    id              uuid         NOT NULL,
    tenant_id       uuid         NOT NULL,
    alarm_id        uuid         NOT NULL,
    device_alarm_id uuid         NOT NULL, -- Id của thiết bị xảy ra cảnh báo của tb device
    device_type     varchar(255) NOT NULL,
    "type"          varchar(45)  NOT NULL, -- loai cb
    viewed          bool,
    created_time    bigint,
    CONSTRAINT cl_alarm_history_pkey PRIMARY KEY (id),
    CONSTRAINT fk_alarmhistory_alarm FOREIGN KEY (alarm_id) REFERENCES alarm (id)
);

CREATE TABLE IF NOT EXISTS cl_camera_record
(
    id               uuid NOT NULL,
    box_id           uuid,
    camera_id        uuid,
    video_url        varchar(4000),
    static_path      varchar(4000),
    size             bigint,
    start_video_time bigint,
    end_video_time   bigint,
    created_time     bigint,
    CONSTRAINT cl_camera_record_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cl_camera_stream_viewer
(
    id         uuid NOT NULL,
    tb_user_id uuid,
    camera_id  uuid,
    status     bool,
    last_ping  int8,
    CONSTRAINT cl_camera_stream_viewer_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cl_camera_rtsp_template
(
    id            uuid NOT NULL,
    manufacture   varchar(255),
    rtsp_template varchar(4000),
    created_time  int8,
    created_by    uuid,
    updated_time  int8,
    updated_by    uuid,
    CONSTRAINT cl_camera_rtsp_template_pkey PRIMARY KEY (id)
);

-- end here

CREATE OR REPLACE PROCEDURE cleanup_events_by_ttl(IN ttl bigint, IN debug_ttl bigint, INOUT deleted bigint)
    LANGUAGE plpgsql AS
$$
DECLARE
    ttl_ts                  bigint;
    debug_ttl_ts            bigint;
    ttl_deleted_count       bigint DEFAULT 0;
    debug_ttl_deleted_count bigint DEFAULT 0;
BEGIN
    IF ttl > 0 THEN
        ttl_ts := (EXTRACT(EPOCH FROM current_timestamp) * 1000 - ttl::bigint * 1000)::bigint;
        EXECUTE format(
                'WITH deleted AS (DELETE FROM event WHERE ts < %L::bigint AND (event_type != %L::varchar AND event_type != %L::varchar) RETURNING *) SELECT count(*) FROM deleted',
                ttl_ts, 'DEBUG_RULE_NODE', 'DEBUG_RULE_CHAIN') into ttl_deleted_count;
    END IF;
    IF debug_ttl > 0 THEN
        debug_ttl_ts := (EXTRACT(EPOCH FROM current_timestamp) * 1000 - debug_ttl::bigint * 1000)::bigint;
        EXECUTE format(
                'WITH deleted AS (DELETE FROM event WHERE ts < %L::bigint AND (event_type = %L::varchar OR event_type = %L::varchar) RETURNING *) SELECT count(*) FROM deleted',
                debug_ttl_ts, 'DEBUG_RULE_NODE', 'DEBUG_RULE_CHAIN') into debug_ttl_deleted_count;
    END IF;
    RAISE NOTICE 'Events removed by ttl: %', ttl_deleted_count;
    RAISE NOTICE 'Debug Events removed by ttl: %', debug_ttl_deleted_count;
    deleted := ttl_deleted_count + debug_ttl_deleted_count;
END
$$;

CREATE OR REPLACE FUNCTION to_uuid(IN entity_id varchar, OUT uuid_id uuid) AS
$$
BEGIN
    uuid_id := substring(entity_id, 8, 8) || '-' || substring(entity_id, 4, 4) || '-1' || substring(entity_id, 1, 3) ||
               '-' || substring(entity_id, 16, 4) || '-' || substring(entity_id, 20, 12);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE cleanup_edge_events_by_ttl(IN ttl bigint, INOUT deleted bigint)
    LANGUAGE plpgsql AS
$$
DECLARE
    ttl_ts            bigint;
    ttl_deleted_count bigint DEFAULT 0;
BEGIN
    IF ttl > 0 THEN
        ttl_ts := (EXTRACT(EPOCH FROM current_timestamp) * 1000 - ttl::bigint * 1000)::bigint;
        EXECUTE format(
                'WITH deleted AS (DELETE FROM edge_event WHERE ts < %L::bigint RETURNING *) SELECT count(*) FROM deleted',
                ttl_ts) into ttl_deleted_count;
    END IF;
    RAISE NOTICE 'Edge events removed by ttl: %', ttl_deleted_count;
    deleted := ttl_deleted_count;
END
$$;
