package me.performancereservation.global.security.admin

import me.performancereservation.domain.admin.Admin
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AdminUserDetails(
    private val admin: Admin
) : UserDetails {

    fun getAdmin(): Admin = admin

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${admin.role.name}"))
    }

    override fun getPassword(): String = admin.password

    override fun getUsername(): String = admin.id
}