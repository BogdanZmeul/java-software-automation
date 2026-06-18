import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock
    AccountRepository accountRepository;

    @Mock
    CommissionService commissionService;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    TransferService transferService;

    @Test
    void shouldExecuteTransferSuccessfully() {
        Account source = new Account(1L, "John Pork", 1000.0, false);
        Account destination = new Account(2L, "Pes Patron", 500.0, false);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(commissionService.calculateCommission(200.0)).thenReturn(10.0);

        Transaction result = transferService.executeTransfer(1L, 2L, 200.0);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(790.0, source.getBalance(), "Source balance should decrease by amount + commission");
        assertEquals(700.0, destination.getBalance(), "Destination balance should increase by amount");

        verify(accountRepository, times(1)).updateBalance(source);
        verify(accountRepository, times(1)).updateBalance(destination);

        verify(commissionService).calculateCommission(200.0);
        verify(commissionService, times(1)).calculateCommission(200.0);

        verify(accountRepository).saveTransaction(any(Transaction.class));

        verify(notificationService).sendNotification(1L, "Sent money to Pes Patron");
        verify(notificationService).sendNotification(2L, "Received money from John Pork");

        verify(notificationService, never()).alertSecurityTeam(anyString());
    }

    @Test
    void shouldThrowExceptionWhenSourceAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.executeTransfer(99L, 2L, 100.0)
        );

        assertEquals("Source account not found", exception.getMessage());

        verify(accountRepository, never()).updateBalance(any());
        verify(accountRepository, never()).saveTransaction(any());
        verify(notificationService, never()).sendNotification(anyLong(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenNotEnoughFunds() {
        Account source = new Account(1L, "John Pork", 50.0, false);
        Account destination = new Account(2L, "Pes Patron", 500.0, false);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(commissionService.calculateCommission(200.0)).thenReturn(10.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.executeTransfer(1L, 2L, 200.0)
        );

        assertEquals("Not enough funds in source account", exception.getMessage());
        assertEquals(50.0, source.getBalance(), "Balance should remain unchanged");

        verify(notificationService, times(1)).sendNotification(1L, "Transfer failed: not enough funds");
        verifyNoMoreInteractions(notificationService);
        verify(accountRepository, never()).updateBalance(any());
    }

    @Test
    void shouldAlertSecurityTeamOnLargeTransfer() {
        Account source = new Account(1L, "John Pork", 20000.0, false);
        Account destination = new Account(2L, "Pes Patron", 500.0, false);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(commissionService.calculateCommission(15000.0)).thenReturn(150.0);

        transferService.executeTransfer(1L, 2L, 15000.0);

        verify(notificationService, times(1)).alertSecurityTeam("Large money transfer detected from account: 1");
    }

    @Test
    void shouldCreateValidTransactionWithSoftAssertions() {
        Account source = new Account(1L, "John Pork", 1000.0, false);
        Account destination = new Account(2L, "Pes Patron", 500.0, false);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(commissionService.calculateCommission(200.0)).thenReturn(10.0);

        Transaction result = transferService.executeTransfer(1L, 2L, 200.0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.getSourceAccountId()).isEqualTo(1L);
        softly.assertThat(result.getDestinationAccountId()).isEqualTo(2L);
        softly.assertThat(result.getAmount()).isEqualTo(200.0);
        softly.assertThat(result.getStatus()).isEqualTo("SUCCESS");
        softly.assertThat(result.getTimestamp()).isNotNull();

        softly.assertAll();
    }

    @Test
    void shouldReturnTransactionHistoryWithAssertJListChecks() {
        Transaction t1 = new Transaction(101L, 1L, 2L, 50.0, "SUCCESS", Instant.now());
        Transaction t2 = new Transaction(102L, 1L, 3L, 100.0, "SUCCESS", Instant.now());
        Transaction t3 = new Transaction(103L, 2L, 1L, 500.0, "FAILED", Instant.now());

        when(accountRepository.findTransactionsByAccountId(1L)).thenReturn(List.of(t1, t2, t3));

        List<Transaction> history = transferService.getHistory(1L);

        assertThat(history)
                .isNotNull()
                .hasSize(3);

        assertThat(history)
                .extracting(Transaction::getAmount)
                .containsExactly(50.0, 100.0, 500.0);

        assertThat(history)
                .filteredOn(t -> "SUCCESS".equals(t.getStatus()))
                .extracting(Transaction::getId, Transaction::getDestinationAccountId)
                .containsExactlyInAnyOrder(
                        tuple(101L, 2L),
                        tuple(102L, 3L)
                );
    }

}