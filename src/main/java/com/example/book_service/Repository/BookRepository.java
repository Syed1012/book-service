package com.example.book_service.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.book_service.model.Book;

public interface BookRepository extends MongoRepository<Book, String> {

    // Find books by author
    List<Book> findByAuthor(String author);

    // Find Books published in a specific year
    List<Book> findByYear(int year);

    // Find Book by Id
    Optional<Book> findById(String id);

}