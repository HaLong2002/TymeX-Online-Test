package com.company;

import java.util.Arrays;
import java.util.Scanner;

public class Main {

    static int n;
    static int[] array;

    private static void nhap() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Nhap n: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Nhap lai n (n phai la so nguyen):");
            scanner.next();
        }
        n = scanner.nextInt();

        array = new int[n];
        System.out.println("Nhap vao " + n + " so trong khoang tu 1 den " + (n + 1) +  ": ");
        for (int i = 0; i < n; i++) {
            if (scanner.hasNextInt()) {
                array[i] = scanner.nextInt();
            } else {
                System.out.println("Vui long chi nhap so nguyen.");
                scanner.next();
                i--;
            }
        }

        scanner.close();
    }

    private static int FindTheMissingNumber() {
        int totalSum = (n + 1) * (n + 2) / 2;
        int arraySum = Arrays.stream(array).sum();
        return totalSum - arraySum;
    }

    public static void main(String[] args) {
        nhap();
        System.out.println(FindTheMissingNumber());
    }
}
