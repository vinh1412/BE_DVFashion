/*
 * @ {#} User.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.enums.Gender;
import vn.edu.iuh.fit.enums.TypeProviderAuth;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/*
 * @description: Entity class representing a user in the application
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone", unique = true)
    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dob;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active= true;

    @Column(name = "created_at")
    private LocalDateTime createAt;

    @Column(name = "updated_at")
    private LocalDateTime updateAt;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<TypeProviderAuth> typeProviderAuths = new HashSet<>();

    @Column(name = "provider_id")
    private String providerId;

//    @OneToMany(mappedBy = "user")
//    private List<Address> addresses= new ArrayList<>();
//
//    @OneToMany(mappedBy = "user")
//    private List<Order> orders= new ArrayList<>();
//
//    @OneToOne(mappedBy = "user")
//    private ShoppingCart cart;
//
//    @OneToMany(mappedBy = "user")
//    private List<Review> reviews= new ArrayList<>();
//
//    @OneToMany(mappedBy = "user")
//    private List<WishlistItem> wishlist= new ArrayList<>();

//    @OneToMany(mappedBy = "user")
//    private List<Token> tokens = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles= new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
