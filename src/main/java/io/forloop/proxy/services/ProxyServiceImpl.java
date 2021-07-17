package io.forloop.proxy.services;

import io.forloop.proxy.domain.Count;
import io.forloop.proxy.domain.Proxy;
import io.forloop.proxy.repositories.ProxyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyServiceImpl implements ProxyService {

    private final ProxyRepository repository;

    @Override
    public Proxy leaseProxy() {
        final var first = repository.findFirstByValidatedTrueAndInUseFalse();
        first.setInUse(true);

        return repository.save(first);
    }

    @Override
    @Cacheable(cacheNames = "proxies")
    public List<Proxy> findAll() {
        return repository.findAll();
    }

    @Override
    @CacheEvict(cacheNames = "proxies", allEntries = true)
    public List<Proxy> saveAllFromFile(final MultipartFile file) throws IOException {

        // Convert the bytes into a list of strings
        final var strings = new ArrayList<>(List.of(new String(file.getBytes(), StandardCharsets.UTF_8).split("\n")));

        // Convert strings into Proxy objects
        final var results = strings.stream().map(proxyString -> Proxy
                .builder()
                .value(proxyString)
                .build())
                .collect(Collectors.toCollection(ArrayList::new));

        return repository.saveAll(results);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public void deleteById(final String id) {
        repository.deleteById(id);
    }

    @Override
    public void delete(final Proxy proxy) {
        repository.delete(proxy);
    }

    @Override
    public Proxy markValid(final Proxy proxy) {
        proxy.setValidated(true);

        return repository.save(proxy);
    }

    @Override
    public Count count() {

        return Count.builder()
                .validatedTrue(repository.countAllByValidatedTrue())
                .validatedFalse(repository.countAllByValidatedFalse())
                .build();
    }
}