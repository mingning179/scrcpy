package com.nothing.dpl;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

public class DataPreparation {
    public static DataSet generateDataSet() {
        final int count=10000;
        final int maxVal=1000;
        // 生成更多的加法运算数据（100以内）
        double[][] inputs = new double[count][2];
        double[][] outputs = new double[count][1];

        for (int i = 0; i < count; i++) {
            double a = (int) (Math.random() * maxVal);
            double b = (int) (Math.random() * maxVal);
            inputs[i][0] = a;
            inputs[i][1] = b;
            outputs[i][0] = a + b;
        }

        INDArray inputMatrix = Nd4j.create(inputs);
        INDArray outputMatrix = Nd4j.create(outputs);

        return new DataSet(inputMatrix, outputMatrix);
    }
}