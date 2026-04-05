/**
 * DDD Gym - 숙박 예약 도메인 예시 (Java)
 *
 * 이 코드는 완전한 구현이 아닌 설계 의도를 보여주는 스케치입니다.
 * 클래스 구조와 불변조건 표현에 집중하세요.
 */

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// ============================================
// Value Objects
// ============================================

/** 예약 기간 (Value Object) */
record StayPeriod(LocalDate checkInDate, LocalDate checkOutDate) {
    public StayPeriod {
        Objects.requireNonNull(checkInDate, "체크인 날짜는 필수입니다");
        Objects.requireNonNull(checkOutDate, "체크아웃 날짜는 필수입니다");
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜 이후여야 합니다");
        }
    }

    public boolean overlaps(StayPeriod other) {
        return !(checkOutDate.compareTo(other.checkInDate) <= 0
              || checkInDate.compareTo(other.checkOutDate) >= 0);
    }

    public int nights() {
        return (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}

/** 인원 수 (Value Object) */
record GuestCount(int adults, int children) {
    public GuestCount {
        if (adults < 1) {
            throw new IllegalArgumentException("최소 1명의 성인이 필요합니다");
        }
        if (children < 0) {
            throw new IllegalArgumentException("어린이 수는 0 이상이어야 합니다");
        }
    }

    public GuestCount(int adults) {
        this(adults, 0);
    }

    public int total() {
        return adults + children;
    }
}

/** 예약 ID (Value Object) */
record BookingId(String value) {
    public BookingId {
        Objects.requireNonNull(value, "예약 ID는 필수입니다");
        if (value.isBlank()) {
            throw new IllegalArgumentException("예약 ID는 비어있을 수 없습니다");
        }
    }
}

// ============================================
// Entity & Aggregate Root
// ============================================

/** 예약 상태 */
enum BookingStatus {
    REQUESTED,   // 요청됨
    CONFIRMED,   // 확정됨
    CANCELLED,   // 취소됨
    CHECKED_IN,  // 체크인
    CHECKED_OUT  // 체크아웃
}

/**
 * 예약 (Aggregate Root)
 *
 * 불변조건:
 * - 취소된 예약은 다시 확정할 수 없다
 * - 체크인 이후에는 취소할 수 없다
 * - 체크인 없이 체크아웃할 수 없다
 */
class Booking {
    private final BookingId id;
    private final String accommodationId;
    private final String guestId;
    private final StayPeriod period;
    private final GuestCount guestCount;
    private BookingStatus status;
    private final List<DomainEvent> events = new ArrayList<>();

    private Booking(BookingId id, String accommodationId, String guestId,
                    StayPeriod period, GuestCount guestCount) {
        this.id = id;
        this.accommodationId = accommodationId;
        this.guestId = guestId;
        this.period = period;
        this.guestCount = guestCount;
        this.status = BookingStatus.REQUESTED;
    }

    public static Booking request(BookingId id, String accommodationId,
                                   String guestId, StayPeriod period,
                                   GuestCount guestCount) {
        var booking = new Booking(id, accommodationId, guestId, period, guestCount);
        booking.events.add(new BookingRequested(id, accommodationId, period));
        return booking;
    }

    /** 예약 확정 */
    public void confirm() {
        // 불변조건: 취소된 예약은 재확정 불가
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("취소된 예약은 다시 확정할 수 없습니다");
        }
        if (status != BookingStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태의 예약만 확정할 수 있습니다");
        }

        this.status = BookingStatus.CONFIRMED;
        this.events.add(new BookingConfirmed(id));
    }

    /** 예약 취소 */
    public void cancel(String reason) {
        // 불변조건: 체크인 이후 취소 불가
        if (status == BookingStatus.CHECKED_IN || status == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("체크인 이후에는 취소할 수 없습니다");
        }
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다");
        }

        this.status = BookingStatus.CANCELLED;
        this.events.add(new BookingCancelled(id, reason));
    }

    /** 체크인 */
    public void checkIn() {
        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 예약만 체크인할 수 있습니다");
        }

        this.status = BookingStatus.CHECKED_IN;
        this.events.add(new CheckedIn(id));
    }

    /** 체크아웃 */
    public void checkOut() {
        // 불변조건: 체크인 없이 체크아웃 불가
        if (status != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("체크인된 예약만 체크아웃할 수 있습니다");
        }

        this.status = BookingStatus.CHECKED_OUT;
        this.events.add(new CheckedOut(id));
    }

    // Getters
    public BookingId getId() { return id; }
    public String getAccommodationId() { return accommodationId; }
    public String getGuestId() { return guestId; }
    public StayPeriod getPeriod() { return period; }
    public GuestCount getGuestCount() { return guestCount; }
    public BookingStatus getStatus() { return status; }
    public List<DomainEvent> getEvents() { return Collections.unmodifiableList(events); }

    public void clearEvents() {
        events.clear();
    }
}

// ============================================
// Domain Events
// ============================================

sealed interface DomainEvent permits BookingRequested, BookingConfirmed,
                                     BookingCancelled, CheckedIn, CheckedOut {}

record BookingRequested(BookingId bookingId, String accommodationId,
                        StayPeriod period) implements DomainEvent {}

record BookingConfirmed(BookingId bookingId) implements DomainEvent {}

record BookingCancelled(BookingId bookingId, String reason) implements DomainEvent {}

record CheckedIn(BookingId bookingId) implements DomainEvent {}

record CheckedOut(BookingId bookingId) implements DomainEvent {}

// ============================================
// 사용 예시
// ============================================

class BookingExample {
    public static void main(String[] args) {
        var booking = Booking.request(
            new BookingId("BK-001"),
            "ACC-001",
            "GUEST-001",
            new StayPeriod(
                LocalDate.of(2024, 7, 20),
                LocalDate.of(2024, 7, 22)
            ),
            new GuestCount(2, 1)
        );

        // 예약 확정
        booking.confirm();

        // 체크인
        booking.checkIn();

        // 체크아웃
        booking.checkOut();

        // 발생한 이벤트 확인
        booking.getEvents().forEach(System.out::println);
    }
}
