/*
 * The MIT License
 *
 * Copyright 2018 Luis Pichio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.luispichio.ajmodbus.examples;

import com.google.gson.Gson;
import com.luispichio.ajmodbus.ModbusMaster;
import com.luispichio.ajmodbus.ModbusMasterListener;
import com.luispichio.ajmodbus.ModbusRequest;
import com.luispichio.ajmodbus.ModbusResponse;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Luis Pichio | luispichio@gmail.com | https://sites.google.com/site/luispichio/ | https://github.com/luispichio
 */
public class ModbusMasterTester implements ModbusMasterListener {
    ModbusMaster mMaster;
    SerialPort mSerial;
    int mStep = 0;
    private final Gson mGson;

    void doRequests(){
//        mMaster.readInputRegisters(100, 0x1027, 0x0D00);
//        mMaster.readHoldingRegisters(100, 0x8403, 0x0600);
        mMaster.writeMultipleCoils(100, 200, 2, new boolean[] {(System.nanoTime() & 0x01) == 0x01, (System.nanoTime() & 0x01) == 0x01});
        mMaster.readCoils(100, 200, 2);
        mMaster.readInputRegisters(100, 10000, 13);
        mMaster.writeSingleRegister(100, 0, (int) (System.nanoTime() & 0xFFFF));
        mMaster.readHoldingRegisters(100, 0, 1);
        mMaster.writeMultipleRegisters(100, 4700, 10, new int[10]);
        mMaster.readHoldingRegisters(100, 4700, 10);
//        mMaster.writeSingleRegister(100, 101, 1);


//        mMaster.writeSingleRegister(100, 0x6400, 1);
//        mMaster.writeSingleRegister(100, 0x6500, 1);
//        mMaster.readHoldingRegisters(100, 0x6400, 0x0200);
//        mMaster.readFileRecord(100, 0, 0, 26 << 8);
//        mMaster.writeSingleCoil(100, 200, 0xFF00);
//        mMaster.readInputRegisters(100, 0, 1);
    }
    
    ModbusMasterTester() throws SerialPortException{
        mGson = new Gson();
        mMaster = new ModbusMaster(this);
        mMaster.setup(300, 50, 300, 3);
        mSerial = new SerialPort("COM1");
        mSerial.openPort();
        mSerial.setParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);        
        while (true){
            if (mMaster.emptyRequestTail())
                doRequests();
            int avaiable = mSerial.getInputBufferBytesCount();
            if (avaiable > 0)
                mMaster.onRX(mSerial.readBytes());
            mMaster.poll();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(ModbusMasterTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String[] args) throws SerialPortException {
        new ModbusMasterTester();    
    }    


    @Override
    public void onRX(byte[] bytes) {
        System.out.print("onRX -> ");
        System.out.println(Arrays.toString(bytes));
    }
    
    @Override
    public void onTX(byte[] bytes) {
        try {
            System.out.print("onTX -> ");
            System.out.println(Arrays.toString(bytes));
            mSerial.writeBytes(bytes);
        } catch (SerialPortException ex) {
            Logger.getLogger(ModbusMasterTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean onModbusResponse(ModbusRequest request, ModbusResponse response) {
        System.out.println("onModbusResponse");
        System.out.println(mGson.toJson(request));
        System.out.println(mGson.toJson(response));
        return false;
    }

    @Override
    public void onModbusException(ModbusRequest request, ModbusResponse response) {
        System.out.println("onModbusException");
        System.out.println(mGson.toJson(request));
        System.out.println(mGson.toJson(response));
    }

    @Override
    public void onModbusTimeOut(ModbusRequest request) {
        System.out.println("onModbusTimeOut");
        System.out.println(mGson.toJson(request));
    }

    @Override
    public boolean onResponseReadCoils(int slaveAddress, int address, int quantity, boolean[] value) {
        System.out.println("onResponseReadCoils");
        System.out.println(String.format("%d %d %d %s", slaveAddress, address, quantity, Arrays.toString(value)));  
        return true;
    }

    @Override
    public boolean onResponseReadHoldingRegisters(int slaveAddress, int address, int quantity, int[] value) {
        System.out.println("onResponseReadHoldingRegisters");
        System.out.println(String.format("%d %d %d %s", slaveAddress, address, quantity, Arrays.toString(value)));
        return true;
    }

    @Override
    public boolean onResponseReadInputRegisters(int slaveAddress, int address, int quantity, int[] value) {
        System.out.println("onResponseReadInputRegisters");
        System.out.println(String.format("%d %d %d %s", slaveAddress, address, quantity, Arrays.toString(value)));
        return true;
    }

    @Override
    public boolean onResponseWriteSingleCoil(int slaveAddress, int address, boolean value) {
        System.out.println("onResponseWriteSingleRegister");
        System.out.println(String.format("%d %d %s", slaveAddress, address, value));       
        return true;
    }

    @Override
    public boolean onResponseWriteSingleRegister(int slaveAddress, int address, int value) {
        System.out.println("onResponseWriteSingleRegister");
        System.out.println(String.format("%d %d %d", slaveAddress, address, value));       
        return true;
    }

    @Override
    public boolean onResponseWriteMultipleCoils(int slaveAddress, int address, int quantity, boolean[] value) {
        System.out.println("onResponseWriteMultipleCoils");
        System.out.println(String.format("%d %d %d %s", slaveAddress, address, quantity, Arrays.toString(value)));
        return true;
    }

    @Override
    public boolean onResponseWriteMultipleRegisters(int slaveAddress, int address, int quantity, int[] value) {
        System.out.println("onResponseWriteMultipleRegisters");
        System.out.println(String.format("%d %d %d %s", slaveAddress, address, quantity, Arrays.toString(value)));
        return true;
    }
}
