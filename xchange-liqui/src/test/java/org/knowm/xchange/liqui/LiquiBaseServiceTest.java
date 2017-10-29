package org.knowm.xchange.liqui;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.liqui.dto.marketdata.result.LiquiDepthResult;
import org.knowm.xchange.liqui.dto.marketdata.result.LiquiTickersResult;
import org.knowm.xchange.liqui.dto.marketdata.result.LiquiTradesResult;

public class LiquiBaseServiceTest {

    @Test
    public void testBaseService() throws Exception {
        final ExchangeSpecification specification = new ExchangeSpecification(LiquiExchange.class);
        final Exchange exchange = ExchangeFactory.INSTANCE.createExchange(specification);
        final LiquiBaseService service = new LiquiBaseService(exchange);

        service.getInfo();
        final LiquiTickersResult ticker = service.getTicker(CurrencyPair.ETH_BTC);
        final LiquiTickersResult tickers = service.getTicker(Lists.newArrayList(CurrencyPair.LTC_BTC, CurrencyPair.ETH_BTC));
        final LiquiDepthResult depth = service.getDepth(Lists.newArrayList(CurrencyPair.ETH_BTC, CurrencyPair.LTC_BTC), 3);

        final LiquiTradesResult trades = service.getTrades(Lists.newArrayList(CurrencyPair.ETH_BTC, CurrencyPair.LTC_BTC), 3);

    }
}
