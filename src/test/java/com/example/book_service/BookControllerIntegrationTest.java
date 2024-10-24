package com.example.book_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.book_service.model.Book;
import com.example.book_service.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BookControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookService bookService;

    private Book testBook;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        // Clearing the database before each test
        bookService.getAllBooks().forEach(book -> bookService.deleteBook(book.getId()));

        // Create a test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setPrice(29.99);
        testBook.setYear(2024);
    }

    @Test
    void createBook_ShouldCreateBookSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/books/addnew-book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isCreated())
                .andReturn();

        Book createdBook = objectMapper.readValue(
                result.getResponse().getContentAsString(), Book.class);

        assertNotNull(createdBook.getId());
        assertEquals(testBook.getTitle(), createdBook.getTitle());
        assertEquals(testBook.getAuthor(), createdBook.getAuthor());
        assertEquals(testBook.getPrice(), createdBook.getPrice());
        assertEquals(testBook.getYear(), createdBook.getYear());
    }

    @Test
    void updateBook_WithValidId_ShouldUpdateBook() throws Exception {
        Book savedBook = bookService.addBook(testBook);
        
        Book updatedBook = new Book();
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor(testBook.getAuthor());
        updatedBook.setPrice(39.99);
        updatedBook.setYear(2024);

        mockMvc.perform(put("/api/books/updateBook-id/{id}", savedBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.price").value(39.99));
    }

    @Test
    void deleteBook_WithValidId_ShouldDeleteBook() throws Exception {
        Book savedBook = bookService.addBook(testBook);

        mockMvc.perform(delete("/api/books/deletebook-id/{id}", savedBook.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/getSpecific-book-id/{id}", savedBook.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBooksByAuthor_ShouldReturnMatchingBooks() throws Exception {
        bookService.addBook(testBook);
        Book anotherBook = new Book(null, "Another Book", testBook.getAuthor(), 24.99, 2023);
        bookService.addBook(anotherBook);

        MvcResult result = mockMvc.perform(get("/api/books/searchby-author/{author}", testBook.getAuthor()))
                .andExpect(status().isOk())
                .andReturn();

        List<?> books = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));

        assertEquals(2, books.size());
    }

    @Test
    void getBooksByYear_ShouldReturnMatchingBooks() throws Exception {
        bookService.addBook(testBook);
        Book anotherBook = new Book(null, "Another Book", "Different Author", 24.99, testBook.getYear());
        bookService.addBook(anotherBook);

        MvcResult result = mockMvc.perform(get("/api/books/searchby-year/{year}", testBook.getYear()))
                .andExpect(status().isOk())
                .andReturn();

        List<?> books = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));

        assertEquals(2, books.size());
    }
}