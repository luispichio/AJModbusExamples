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

import com.luispichio.ajmodbus.ModbusExceptionResponse;
import com.luispichio.ajmodbus.ModbusRequest;
import com.luispichio.ajmodbus.ModbusResponse;
import com.luispichio.ajmodbus.ModbusSlave;
import com.luispichio.ajmodbus.ModbusSlaveListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Luis Pichio | luispichio@gmail.com | https://sites.google.com/site/luispichio/ | https://github.com/luispichio
 */
public class ModbusSlaveTester implements ModbusSlaveListener {
    ModbusSlave mSlave;
    SerialPort mSerial;
    int mStep = 0;
    int[] mHoldingRegisters = new int[65536];
    int[] mInputRegisters = new int[65536];
    boolean[] mCoils = new boolean[65536];
    
    ModbusSlaveTester() throws SerialPortException {
        mSlave = new ModbusSlave(this);
        mSlave.setup(100, 10);
        mSerial = new SerialPort("COM2");
        mSerial.openPort();
        mSerial.setParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);        
        while (true){
            if (mSerial.getInputBufferBytesCount() > 0){
                mSlave.onRX(mSerial.readBytes());
            }
            mSlave.poll();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(ModbusSlaveTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String[] args) throws SerialPortException {
        new ModbusSlaveTester();    
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
    public ModbusResponse onRequest(ModbusRequest request) {
        return null;
    }

    @Override
    public ModbusResponse onReadCoils(int slaveAddress, int function, int address, int quantity) {
        System.out.println("onReadCoils(" + slaveAddress + " " + address + " " + quantity + ")");
        return ModbusResponse.readCoils(slaveAddress, address, quantity, Arrays.copyOfRange(mCoils, address, address + quantity));
//        return ModbusResponse.exception(slaveAddress, function, ModbusExceptionResponse.ILLEGAL_FUNCTION);        
    }

    @Override
    public ModbusResponse onReadHoldingRegisters(int slaveAddress, int function, int address, int quantity) {
        System.out.println("onReadHoldingRegisters(" + slaveAddress + " " + address + " " + quantity + ")");
        return ModbusResponse.readHoldingRegisters(slaveAddress, address, quantity, Arrays.copyOfRange(mHoldingRegisters, address, address + quantity));
    }

    @Override
    public ModbusResponse onReadInputRegisters(int slaveAddress, int function, int address, int quantity) {
        System.out.println("onReadInputRegisters(" + slaveAddress + " " + address + " " + quantity + ")");
        return ModbusResponse.readInputRegisters(slaveAddress, address, quantity, Arrays.copyOfRange(mInputRegisters, address, address + quantity));
    }

    @Override
    public ModbusResponse onWriteSingleCoil(int slaveAddress, int function, int address, boolean value) {
        System.out.println("onWriteSingleCoil(" + slaveAddress + " " + address + " " + value + ")");
        mCoils[address] = value;
        return ModbusResponse.writeSingleCoil(slaveAddress, address, value);        
    }

    @Override
    public ModbusResponse onWriteSingleRegister(int slaveAddress, int function, int address, int value) {
        System.out.println("onWriteSingleRegister(" + slaveAddress + " " + address + " " + value + ")");
        mHoldingRegisters[address] = value;
        return ModbusResponse.writeSingleRegister(slaveAddress, address, value);
    }

    @Override
    public ModbusResponse onWriteMultipleRegisters(int slaveAddress, int function, int address, int quantity, int[] values) {
        System.out.println("onWriteMultipleRegisters(" + slaveAddress + " " + address + " " + quantity + " " + Arrays.toString(values) + ")");
        System.arraycopy(values, 0, mHoldingRegisters, address, quantity);
        return ModbusResponse.writeMultipleRegisters(slaveAddress, address, quantity);
    }

    @Override
    public ModbusResponse onWriteMultipleCoils(int slaveAddress, int function, int address, int quantity, boolean[] values) {
        System.out.println("onWriteMultipleCoils(" + slaveAddress + " " + address + " " + quantity + " " + Arrays.toString(values) + ")");
        System.arraycopy(values, 0, mCoils, address, quantity);
        return ModbusResponse.writeMultipleCoils(slaveAddress, address, quantity);
    }
}
