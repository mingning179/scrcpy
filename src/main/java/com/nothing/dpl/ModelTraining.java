package com.nothing.dpl;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.*;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public class ModelTraining {
    public static void main(String[] args) {
        // 设置ND4J使用GPU
        System.setProperty("org.nd4j.linalg.factory.Nd4jBackend", "org.nd4j.linalg.jcublas.JCublasBackend");

        // 模型文件
        File modelFile = new File("model.zip");

        MultiLayerNetwork model;
        boolean awaysTrain = false;
        if (modelFile.exists() && !awaysTrain) {
            // 加载已有模型
            try {
                model = ModelSerializer.restoreMultiLayerNetwork(modelFile);
                System.out.println("模型加载成功！");
            } catch (IOException e) {
                throw new RuntimeException("无法加载模型", e);
            }
        } else {
            // 准备数据
            DataSet dataSet = DataPreparation.generateDataSet();

            int hiddenLayerCount = 10;
            int hiddenLayerInputSize = 2;
            int hideenLayerSize = 64;


            NeuralNetConfiguration.ListBuilder listBuilder = new NeuralNetConfiguration.Builder()
                    .seed(123)
                    .updater(new AMSGrad()) // 使用Adam优化器
                    .weightInit(WeightInit.XAVIER)
                    .l2(0.0001)
                    .list();
            for (int i = 0; i < hiddenLayerCount; i++) {
                if (i != 0) {
                    hiddenLayerInputSize = hideenLayerSize;
                }
                listBuilder.layer(new DenseLayer.Builder().nIn(hiddenLayerInputSize).nOut(hideenLayerSize).activation(Activation.RELU).build());
            }

            listBuilder.layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                    .activation(Activation.IDENTITY)
                    .nIn(hideenLayerSize).nOut(1).build());

            // 构建模型
            model = new MultiLayerNetwork(listBuilder.backpropType(BackpropType.Standard).build());

            model.init();

            int listenerFreq = 100;
            model.setListeners(new ScoreIterationListener(listenerFreq){
                double last_score=Double.MAX_VALUE;
                @Override
                public void iterationDone(Model model, int iteration, int epoch) {
                    super.iterationDone(model, iteration, epoch);
                    double score=model.score();
                    if (iteration % listenerFreq == 0) {
                        if(score<last_score){
                            last_score=score;
                            System.out.println("当前模型得分："+score);
                            try {
                                ModelSerializer.writeModel(model, modelFile, true);
                                System.out.println("模型保存成功！");
                            } catch (IOException e) {
                                throw new RuntimeException("无法保存模型", e);
                            }
                        }
                    }

                }
            });

            // 训练模型
            for (int i = 0; i < 100000; i++) {
                model.fit(dataSet);
            }

        }

        //测试模型
        int maxValue = 1111;
        double sumError=0;
        for (int i = 0; i < 1000; i++) {
            double a = (int) (Math.random() * maxValue);
            double b = (int) (Math.random() * maxValue);

            INDArray input = Nd4j.create(new double[]{a, b}, new int[]{1, 2});
            INDArray output = model.output(input);

            //获取测试结果，4舍5入
            double outValue = output.getDouble(0);
            double realValue = a + b;
            double error = Math.abs(outValue - realValue);

            sumError+=error;
            if (error > 0.0) {
                System.out.printf("%f + %f 测试结果：%f，正确结果：%f，误差：%f%n", a, b, outValue, a + b, error);
            }
        }
        //输出测试误差
        System.out.println("测试误差："+sumError);

    }
}