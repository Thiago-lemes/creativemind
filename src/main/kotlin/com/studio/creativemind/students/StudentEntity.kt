package com.studio.creativemind.students

import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "students")
class StudentEntity(

    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column
    var phone: String? = null,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Column(nullable = false)
    var active: Boolean = true

) {

    fun activate() {
        this.active = true
    }

    fun deactivate() {
        this.active = false
    }
}
