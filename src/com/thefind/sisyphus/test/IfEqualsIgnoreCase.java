package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfEqualsIgnoreCase
extends TestString
{
  public IfEqualsIgnoreCase(String column, String value) { super(column, value); }

  @Override
  public boolean eval(String that)
  throws EvalException
  { return (value_.equalsIgnoreCase(that)); }

  @Override
  public String toStringWhich()
  { return "=={i}"; }
}

