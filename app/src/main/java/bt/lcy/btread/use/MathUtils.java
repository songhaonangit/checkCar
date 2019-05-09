package bt.lcy.btread.use;

public class MathUtils {




    //均值
    public static double meanAverage(double[] population) {
        double average = 0.0;
        for (double p : population) {
            average += p;
        }

        return average /= population.length;
    }







    //方差
    public static double varianceImperative(double[] population) {
        double average = 0.0;
        for (double p : population) {
            average += p;
        }
        average /= population.length;

        double variance = 0.0;
        for (double p : population) {
            variance += (p - average) * (p - average);
        }
        return variance / population.length;
    }


    //方差s^2=[(x1-x)^2 +...(xn-x)^2]/n
    public static double Variance(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
        }
        double dAve=sum/m;//求平均值
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            dVar+=(x[i]-dAve)*(x[i]-dAve);
        }
        return dVar/m;
    }

    //标准差σ=sqrt(s^2)
    public static double StandardDiviation(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
        }
        double dAve=sum/m;//求平均值
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            dVar+=(x[i]-dAve)*(x[i]-dAve);
        }
        return Math.sqrt(dVar/m);
    }

}
