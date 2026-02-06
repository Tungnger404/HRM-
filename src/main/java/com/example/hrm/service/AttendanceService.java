import com.example.hrm.entity.AttendanceLog;
import com.example.hrm.repository.AttendanceLogRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AttendanceService {
    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    public void checkIn(AttendanceLog log) {
        // Ví dụ logic: Nếu check-in sau 8:00 thì set status = 'LATE'
        if (log.getCheckIn().getHour() >= 8) {
            log.setStatus("LATE");
        } else {
            log.setStatus("ON_TIME");
        }
        attendanceLogRepository.save(log);
    }

    public List<AttendanceLog> getAllLogs() {
        return attendanceLogRepository.findAll();
    }
}