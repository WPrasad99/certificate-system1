package com.certificate.service;

import com.certificate.config.SupabaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final SupabaseConfig supabaseConfig;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Upload a file to Supabase storage
     * 
     * @param fileData The file bytes to upload
     * @param filename The filename (path) in the bucket
     * @return The public URL of the uploaded file
     */
    public String uploadFile(byte[] fileData, String filename) throws IOException, InterruptedException {
        if (!supabaseConfig.isConfigured()) {
            log.warn("Supabase not configured, skipping upload for: {}", filename);
            return null;
        }

        String uploadUrl = supabaseConfig.getUploadUrl() + "/" + filename;

        log.info("Uploading to Supabase: {}", uploadUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                .header("Content-Type", "image/png")
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileData))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            String publicUrl = supabaseConfig.getStorageUrl() + "/" + filename;
            log.info("Successfully uploaded to Supabase: {}", publicUrl);
            return publicUrl;
        } else {
            log.error("Failed to upload to Supabase. Status: {}, Body: {}", response.statusCode(), response.body());
            throw new IOException("Failed to upload to Supabase: " + response.body());
        }
    }

    /**
     * Delete a file from Supabase storage
     * 
     * @param filename The filename (path) in the bucket
     */
    public void deleteFile(String filename) throws IOException, InterruptedException {
        if (!supabaseConfig.isConfigured()) {
            log.warn("Supabase not configured, skipping delete for: {}", filename);
            return;
        }

        String deleteUrl = supabaseConfig.getUploadUrl() + "/" + filename;

        log.info("Deleting from Supabase: {}", deleteUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .header("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                .DELETE()
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 204) {
            log.info("Successfully deleted from Supabase: {}", filename);
        } else {
            log.error("Failed to delete from Supabase. Status: {}, Body: {}", response.statusCode(), response.body());
            throw new IOException("Failed to delete from Supabase: " + response.body());
        }
    }

    /**
     * Check if Supabase storage is configured
     */
    public boolean isConfigured() {
        return supabaseConfig.isConfigured();
    }
}
