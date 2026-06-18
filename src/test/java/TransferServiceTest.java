import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;

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

}