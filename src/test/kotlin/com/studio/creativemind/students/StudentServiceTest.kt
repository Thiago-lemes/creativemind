package com.studio.creativemind.students

import com.studio.creativemind.common.BusinessException
import com.studio.creativemind.students.dto.CreateStudentRequest
import com.studio.creativemind.students.mapper.StudentMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StudentServiceTest {

    private lateinit var studentRepository: StudentRepository
    private lateinit var studentService: StudentService
    private val mapper = StudentMapper()


    @BeforeEach
    fun setup() {
        studentRepository = mockk()
        studentService = StudentService(studentRepository, mapper)
    }

    @Test
    fun `should create student successfully`() {
        val request = CreateStudentRequest(
            name = "Thiago",
            email = "thiago@email.com",
            phone = "41999999999",
            birthDate = LocalDate.of(1997, 4, 28)
        )

        every { studentRepository.existsByEmail(request.email) } returns false

        every { studentRepository.save(any()) } answers {
            val student = firstArg<StudentEntity>()
            student.id = UUID.randomUUID()
            student
        }

        val result = studentService.create(request)

        assertNotNull(result.id)
        assertEquals("Thiago", result.name)
        assertEquals("thiago@email.com", result.email)
        assertTrue(result.active)

        verify(exactly = 1) { studentRepository.existsByEmail(request.email) }
        verify(exactly = 1) { studentRepository.save(any()) }
    }


    @Test
    fun `should throw exception when email already exists`() {
        val request = CreateStudentRequest(
            name = "Thiago",
            email = "thiago@email.com",
            phone = null,
            birthDate = null
        )

        every { studentRepository.existsByEmail(request.email) } returns true

        assertThrows<BusinessException> {
            studentService.create(request)
        }

        verify(exactly = 0) { studentRepository.save(any()) }
    }

    @Test
    fun `should find student by id`() {
        val id = UUID.randomUUID()

        val student = StudentEntity(
            id = id,
            name = "Thiago",
            email = "thiago@email.com",
            phone = null,
            birthDate = null,
            active = true
        )

        every { studentRepository.findById(id) } returns Optional.of(student)

        val result = studentService.findById(id)

        assertEquals(id, result.id)
        assertEquals("Thiago", result.name)

        verify(exactly = 1) { studentRepository.findById(id) }
    }

    @Test
    fun `should throw when student not found`() {
        val id = UUID.randomUUID()

        every { studentRepository.findById(id) } returns Optional.empty()

        assertThrows<BusinessException> {
            studentService.findById(id)
        }
    }


}
