package org.learning.ml.tests;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.learning.ml.MlApplication;
import org.learning.ml.algorithms.impl.SingleLinearRegressionModel;
import org.learning.ml.dtos.BitcoinPriceDto;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SingleValueLinearRegression {

    public static void main(String[] args) throws IOException {
        Logger.getGlobal().setLevel(Level.ALL);

        final var csv = new CsvMapper();
        final var schema = CsvSchema
                .emptySchema()
                .withHeader();

        // read the CSV
        final MappingIterator<BitcoinPriceDto> content = csv.readerFor(BitcoinPriceDto.class)
                .with(schema)
                .readValues(MlApplication.class.getResourceAsStream("/bitcoin-prices.csv"));

        // get the historical prices
        final var bitcoinPrices = content.readAll();

        // test the models - either train one from scratch or use existing values from previous training sessions
        trainAndTest(bitcoinPrices);
        //useInitializedModel(bitcoinPrices);
    }

    private static void useInitializedModel(final Collection<BitcoinPriceDto> bitcoinPrices) {
        // manually pass the information for the parameters
        final var regressionModel = new SingleLinearRegressionModel(BigDecimal.valueOf(2.999071),
                BigDecimal.valueOf(-60566889.443382));

        guessUsingModel(regressionModel);
    }

    private static void trainAndTest(final Collection<BitcoinPriceDto> bitcoinPrices) {
        final var regressionModel = new SingleLinearRegressionModel();

        regressionModel.learn(bitcoinPrices, bitcoinPrices.parallelStream()
                .map(BitcoinPriceDto::getPrice)
                .collect(Collectors.toList()));

        guessUsingModel(regressionModel);
    }

    private static void guessUsingModel(final SingleLinearRegressionModel regressionModel) {
        final var predictionFuture = regressionModel.predict(new BitcoinPriceDto("2022-01-01"));
        System.out.println("The future prediction is " + predictionFuture);

        final var predictionExisting = regressionModel.predict(new BitcoinPriceDto("2020-11-04"));
        System.out.println("The guess for existing date is " + predictionExisting);
    }
}