package com.example.book_service.service;

import com.example.book_service.model.Book;
import com.example.book_service.Repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

    public Book addBook(Book book) {
        validateBook(book);
        return bookRepository.save(book);
    }

    public Book updateBook(String id, Book updatedBook) {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            validateBook(updatedBook);
            updatedBook.setId(id);
            return bookRepository.save(updatedBook);
        } else {
            throw new ValidationException("Book with ID " + id + " not found");
        }
    }

    public void deleteBook(String id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
        } else {
            throw new ValidationException("Book with ID " + id + " not found");
        }
    }

    public List<Book> findBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    public List<Book> findBooksByYear(int year) {
        return bookRepository.findByYear(year);
    }

    // Helper method for basic book validation
    private void validateBook(Book book) {
        if (book.getPrice() < 0) {
            throw new ValidationException("Price cannot be negative");
        }
        if (book.getYear() < 1440) {
            throw new ValidationException("Year must be after 1440");
        }
    }
}
