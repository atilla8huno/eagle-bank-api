package com.eaglebank.repository;

import com.eaglebank.domain.IdempotencyRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Test suite of the class: IdempotencyRepository")
class IdempotencyRepositoryTest {

    @Autowired
    private IdempotencyRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should persist and find IdempotencyRecord by accountNumber and idempotencyKey")
    void givenIdempotencyRecord_whenSaveAndFind_thenShouldReturnRecord() {
        //given
        Instant now = Instant.parse("2026-10-21T10:00:00Z");
        IdempotencyRecord record = new IdempotencyRecord("01300001", "idem-key-1", "tan-h2001", now);
        repository.saveAndFlush(record);
        entityManager.clear();

        //when
        Optional<IdempotencyRecord> found = repository.findByAccountNumberAndIdempotencyKey("01300001", "idem-key-1");
        Optional<IdempotencyRecord> missing = repository.findByAccountNumberAndIdempotencyKey("01300001", "other-key");

        //then
        assertTrue(found.isPresent());
        assertNotNull(found.get().getId());
        assertEquals("tan-h2001", found.get().getTransactionId());
        assertFalse(missing.isPresent());
    }
}
