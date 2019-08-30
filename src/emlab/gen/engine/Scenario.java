/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.engine;


/**
 *
 * @author ejlchappin
 */
public interface Scenario {
    public String getName();
    public void setName(String name);
    public void build(Schedule schedule);
}
