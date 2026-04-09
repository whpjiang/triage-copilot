package com.example.triagecopilot.service;

import com.example.triagecopilot.dto.TriageAnalyzeRequest;
import com.example.triagecopilot.dto.TriageAnalyzeResponse;

public interface TriageAgentService {

    TriageAnalyzeResponse analyze(TriageAnalyzeRequest request);
}
