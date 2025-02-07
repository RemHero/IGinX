package cn.edu.tsinghua.iginx.statistics.data;

class Scalar {
  private double lower;

  private double upper;

  private int commonPfxLen;

  // 构造函数
  public Scalar(double lower, double upper, int commonPfxLen) {
    this.lower = lower;
    this.upper = upper;
    this.commonPfxLen = commonPfxLen;
  }

  // 获取下限
  public double getLower() {
    return lower;
  }

  // 设置下限
  public void setLower(double lower) {
    this.lower = lower;
  }

  // 获取上限
  public double getUpper() {
    return upper;
  }

  // 设置上限
  public void setUpper(double upper) {
    this.upper = upper;
  }

  // 获取公共前缀长度
  public int getCommonPfxLen() {
    return commonPfxLen;
  }

  // 设置公共前缀长度
  public void setCommonPfxLen(int commonPfxLen) {
    this.commonPfxLen = commonPfxLen;
  }
}
