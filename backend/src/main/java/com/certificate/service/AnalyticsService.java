package com.certificate.service;

import com.certificate.entity.Certificate;
import com.certificate.entity.Event;
import com.certificate.entity.Organizer;
import com.certificate.repository.CertificateRepository;
import com.certificate.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final CertificateRepository certificateRepository;
    private final AuthService authService;

    public Map<String, Object> getStats(String email) {
        Organizer organizer = authService.getOrganizerByEmail(email);
        Long organizerId = organizer.getId();

        List<Event> events = eventRepository.findByOrganizerId(organizerId);
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());

        long eventCount = events.size();
        long certificateCount = 0;
        List<Certificate> certificates = new ArrayList<>();

        if (!eventIds.isEmpty()) {
            certificates = certificateRepository.findByEventIdIn(eventIds);
            certificateCount = certificates.size();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventCount);
        stats.put("totalCertificates", certificateCount);

        // Calculate monthly distribution
        List<Map<String, Object>> monthlyData = calculateMonthlyData(events, certificates);
        stats.put("monthlyData", monthlyData);

        return stats;
    }

    private List<Map<String, Object>> calculateMonthlyData(List<Event> events, List<Certificate> certificates) {
        // Group by month name (Jan, Feb, ...)
        Map<String, Map<String, Integer>> distribution = new LinkedHashMap<>();

        // Initialize last 6 months to ensure we have some data points even if zero
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            Calendar mCal = (Calendar) cal.clone();
            mCal.add(Calendar.MONTH, -i);
            String monthName = mCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
            Map<String, Integer> counts = new HashMap<>();
            counts.put("events", 0);
            counts.put("certs", 0);
            distribution.put(monthName, counts);
        }

        // Fill with real data
        for (Event event : events) {
            if (event.getCreatedAt() != null) {
                String month = event.getCreatedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                if (distribution.containsKey(month)) {
                    distribution.get(month).put("events", distribution.get(month).get("events") + 1);
                }
            }
        }

        for (Certificate cert : certificates) {
            if (cert.getCreatedAt() != null) {
                String month = cert.getCreatedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                if (distribution.containsKey(month)) {
                    distribution.get(month).put("certs", distribution.get(month).get("certs") + 1);
                }
            }
        }

        // Convert to list for frontend
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : distribution.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("events", entry.getValue().get("events"));
            item.put("certs", entry.getValue().get("certs"));
            result.add(item);
        }

        return result;
    }
}
