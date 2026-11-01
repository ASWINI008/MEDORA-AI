package com.medora.backend.service;

import com.medora.backend.entity.Resource;
import com.medora.backend.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private EventPublisherService eventPublisherService;

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public List<Resource> getResourcesByType(String type) {
        if (type == null || type.equalsIgnoreCase("All")) {
            return resourceRepository.findAll();
        }
        return resourceRepository.findAll().stream()
                .filter(r -> r.getType() != null && r.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public Resource saveResource(Resource resource) {
        Resource saved = resourceRepository.save(resource);
        eventPublisherService.publishEvent("resource-update", Map.of(
                "resourceId", saved.getId(),
                "name", saved.getName(),
                "type", saved.getType(),
                "status", saved.getStatus()
        ));
        return saved;
    }

    public Optional<Resource> updateResource(Long id, Resource details) {
        return resourceRepository.findById(id).map(r -> {
            if (details.getName() != null) r.setName(details.getName());
            if (details.getType() != null) r.setType(details.getType());
            if (details.getStatus() != null) r.setStatus(details.getStatus());
            if (details.getUtilizationRate() != null) r.setUtilizationRate(details.getUtilizationRate());
            Resource updated = resourceRepository.save(r);

            eventPublisherService.publishEvent("resource-update", Map.of(
                    "resourceId", updated.getId(),
                    "status", updated.getStatus(),
                    "utilization", updated.getUtilizationRate()
            ));

            return updated;
        });
    }
}
