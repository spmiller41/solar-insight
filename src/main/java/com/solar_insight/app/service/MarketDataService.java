package com.solar_insight.app.service;

import com.solar_insight.app.dao.InMarketZipDAO;
import com.solar_insight.app.dto.InMarketZipDTO;
import com.solar_insight.app.entity.InMarketZip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MarketDataService {

    private final InMarketZipDAO marketDAO;

    @Autowired
    public MarketDataService(InMarketZipDAO marketDAO) {
        this.marketDAO = marketDAO;
    }

    public Optional<InMarketZip> findMarketInfoByZip(InMarketZipDTO zipDTO) {
        Optional<InMarketZip> optMarketData = marketDAO.findByZip(zipDTO);

        if (optMarketData.isPresent()) {
            System.out.println("Zip is in market: " + optMarketData.get());
            return optMarketData;
        } else {
            System.out.println("Zip is out of market: " + zipDTO.getZip());
            return Optional.empty();
        }
    }

}
