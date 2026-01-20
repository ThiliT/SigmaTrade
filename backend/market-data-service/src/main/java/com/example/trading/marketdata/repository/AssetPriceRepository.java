package com.example.trading.marketdata.repository;

import com.example.trading.marketdata.domain.AssetPrice;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetPriceRepository extends JpaRepository<AssetPrice, Long> {
    Optional<AssetPrice> findTopBySymbolOrderByTimestampDesc(String symbol);

    List<AssetPrice> findBySymbolAndTimestampAfterOrderByTimestampAsc(String symbol, Instant after);
}
