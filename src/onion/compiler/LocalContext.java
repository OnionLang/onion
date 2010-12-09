/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Kota Mizushima, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package onion.compiler;

import onion.compiler.util.SymbolGenerator;


/**
 * @author Kota Mizushima
 * Date: 2005/06/28
 */
public class LocalContext {
  private boolean isClosure;
  private boolean isStatic;
  private boolean isGlobal;
  private boolean isMethod;
  private LocalFrame contextFrame;
  private IxCode.MethodRef method;
  private IxCode.ConstructorDefinition constructor;
  private SymbolGenerator generator;

  public LocalContext() {
    this.contextFrame = new LocalFrame(null);
    this.generator = new SymbolGenerator("symbol#");
  }
  
  public boolean isGlobal() {
    return isGlobal;
  }
  
  public void setGlobal(boolean isGlobal) {
    this.isGlobal = isGlobal;
  }

  public void setClosure(boolean isClosure) {
    this.isClosure = isClosure;
  }

  public boolean isClosure() {
    return isClosure;
  }
  
  public boolean isStatic() {
    return isStatic;
  }
  
  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }
  
  public String newName(){
    return generator.generate();
  }
  
  public IxCode.TypeRef returnType(){
    if(isMethod){
      return method.returnType();
    }else{
      return IxCode.BasicTypeRef.VOID;
    }
  }
  
  public IxCode.MethodRef method(){
    return method;
  }
  
  public IxCode.ConstructorRef constructor(){
    return constructor;
  }
  
  public void setMethod(IxCode.MethodRef method) {
    this.method = method;
    this.isMethod = true;
  }
  
  public void setConstructor(IxCode.ConstructorDefinition constructor){
    this.constructor = constructor;
    this.isMethod = false;
  }
  
  public void openFrame(){
    contextFrame = new LocalFrame(contextFrame);
  }
  
  public void closeFrame(){
    contextFrame = contextFrame.parent();
  }
  
  public int depth(){
    if(contextFrame == null){
      return -1;
    }else{
      return contextFrame.depth();
    }
  }
  
  public LocalFrame getContextFrame(){
    return contextFrame;
  }
  
  public void setContextFrame(LocalFrame frame){
    this.contextFrame = frame;
  }
  
  public void openScope() {
    contextFrame.openScope();
  }
  
  public void closeScope() {
    contextFrame.closeScope();
  }
  
  public ClosureLocalBinding lookup(String name) {
    return contextFrame.lookup(name);
  }
  
  public ClosureLocalBinding lookupOnlyCurrentScope(String name){
    return contextFrame.lookupOnlyCurrentScope(name);
  }
  
  public int add(String name, IxCode.TypeRef type) {
    return contextFrame.add(name, type);
  }
  
  public String add(IxCode.TypeRef type){
    String name = newName();
    contextFrame.add(name, type);
    return name;
  }
}
