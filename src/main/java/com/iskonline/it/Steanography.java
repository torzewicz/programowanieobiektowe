/**
 * Copyright © 2014 Leo Xiong <hello@leoxiong.com>
 * Improved  © 2017 by Szymon Bobek <sbobek@agh.edu.pl>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Steganography - to conceal data within other data
 *
 * This implementation supports concealing any form of data within
 * images by utilizing the two least significant bits of each
 * RGB (no alpha) channel of each pixel in the carrier image.
 *
 */
package com.iskonline.it;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

//TODO: problem here
public class Steanography {

    public static void main(String[] args) {

        if (args.length < 2 || args.length > 3) {
            System.out.println("Steganography <input image> <output image> [payload file], switching to interactive mdoe.");
            System.out.println("What you want to do: ");
            System.out.println("1. Encode a payload");
            System.out.println("2. Decode a payload");
            System.out.println("3. Exit");
            System.out.print("? ");
            // TODO: Problem here
            Scanner in  =  new Scanner(System.in);

            int choice = Integer.parseInt(in.nextLine());
            switch(choice){
                case 1:
                    System.out.print("Type full path to the public image file: ");
                    String pathContainer = in.nextLine();
                    System.out.print("Type full path to the output image file: ");
                    String pathEncoded = in.nextLine();
                    System.out.print("Type full path to the secret payload: ");
                    String pathPayload = in.nextLine();
                    args = new String[3];
                    args[0] = pathContainer;
                    args[1] = pathEncoded;
                    args[2] = pathPayload;
                    break;
                case 2:
                    System.out.print("Type full path to the image file wih secret message: ");
                    String pathSecret = in.nextLine();
                    System.out.print("Type full path to the output file: ");
                    String pathDecoded = in.nextLine();
                    args = new String[2];
                    args[0] = pathSecret;
                    args[1] = pathDecoded;
                    break;
                case 3:
                    return;
            }
            in.close();
        }

        Stopwatch stopwatch = new Stopwatch();

        try {
            if (args.length == 3) {
                try {
                    ImageIO.write(encode(ImageIO.read(new File(args[0])), new BitInputStream(new File(args[2]))), "PNG", new File(args[1]));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            } else if (args.length == 2) {
                decode(ImageIO.read(new File(args[0])), new BitOutputStream(new FileOutputStream(args[1])));
            }

            System.out.println(String.format("done %sms", stopwatch.getTime() / 1000000));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    public static BufferedImage encode(BufferedImage carrier, BitInputStream payload) throws Exception {
        for (int y = 0; y < carrier.getHeight(); y++) {
            for (int x = 0; x < carrier.getWidth(); x++) {
                //TODO Problem here
                int pixel = carrier.getRGB(x, y) & 0xFFF0F0F0;
                for (int offset = 16; offset >= 0; offset -= 8) {
                    int bits = payload.readBits(4);
                    if (bits == -1)
                        return carrier;
                    pixel |= bits << offset;
                }

                carrier.setRGB(x, y, pixel);
            }
        }

        throw new Exception("not enough space");
    }

    //TODO: Enhancement possible here
    public static void decode(BufferedImage carrier, BitOutputStream payload) {
        int payloadLength = 0;
        int sizeBitsRemaining = Integer.SIZE;
        for (int y = 0; y < carrier.getHeight(); y++) {
            for (int x = 0; x < carrier.getWidth(); x++) {
                for (int offset = 16; offset >= 0; offset -= 8) {
                    if(sizeBitsRemaining > 0) {
                        payloadLength = (payloadLength << 4);
                        payloadLength |= (carrier.getRGB(x, y) >> offset) & 0xF;
                        sizeBitsRemaining -= 4;
                        if(sizeBitsRemaining == 0) {
                            payloadLength *= Byte.SIZE;
                        }
                    } else if(payloadLength > 0) {
                        payload.write(4, (carrier.getRGB(x, y) >> offset) & 0xF);
                        payloadLength -= 4;
                    }
                }
            }
        }
        payload.close();
    }
}