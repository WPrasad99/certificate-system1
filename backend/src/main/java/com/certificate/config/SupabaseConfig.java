package com.certificate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    @Value("${supabase.bucket:certificates}")
    private String bucketName;

    public String getStorageUrl() {
        if (supabaseUrl == null || supabaseUrl.isEmpty()) {
            return null;
        }
        return supabaseUrl + "/storage/v1/object/public/" + bucketName;
    }

    public String getUploadUrl() {
        if (supabaseUrl == null || supabaseUrl.isEmpty()) {
            return null;
        }
        return supabaseUrl + "/storage/v1/object/" + bucketName;
    }

    public String getSupabaseKey() {
        return supabaseKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public boolean isConfigured() {
        return supabaseUrl != null && !supabaseUrl.isEmpty()
                && supabaseKey != null && !supabaseKey.isEmpty();
    }
}
