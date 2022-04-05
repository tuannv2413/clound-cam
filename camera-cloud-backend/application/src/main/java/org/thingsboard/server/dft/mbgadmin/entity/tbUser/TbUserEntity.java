package org.thingsboard.server.dft.mbgadmin.entity.tbUser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_user")
public class TbUserEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "created_time")
    private long createdTime;

    @Column(name = "additional_info")
    private String additionalInfo;

    @Column(name = "authority")
    private String authority;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "search_text")
    private String searchText;

    @Column(name = "tenant_id")
    private UUID tenantId;
}
