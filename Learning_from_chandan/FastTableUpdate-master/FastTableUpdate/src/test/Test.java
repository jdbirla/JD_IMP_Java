package test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import util.Helper;

public class Test {
	
	public static void main(String[] args) {
		
		
		int r = test_Helper_getChunkedList(27, 9); if(3==r) System.out.println("pass"); else System.out.println("fail");  
		r = test_Helper_getChunkedList(27, 8); if(4==r) System.out.println("pass"); else System.out.println("fail");  
		r = test_parallel_degree(5,3); if(3==r) System.out.println("pass"); else System.out.println("fail"); 
		r = test_parallel_degree(6,3);if(3==r) System.out.println("pass"); else System.out.println("fail"); 
		r =test_parallel_degree(7,3);if(3==r) System.out.println("pass"); else System.out.println("fail"); 
	}
	
	public static int test_parallel_degree(int inloadSz, int parallel_degree){
		int chunk = inloadSz / parallel_degree;
		chunk = chunk * parallel_degree  < inloadSz ? chunk + 1 : chunk; // to do Math.ciel kind of operation. so that each parallel will get load.
		return test_Helper_getChunkedList(inloadSz, chunk);
	}
	
	public static int test_Helper_getChunkedList(int count, int cs){
		
		int sz = count;
		HashSet<String> load = new HashSet<String>(sz);
		for (int i=0; i<sz; i++){
			Random r = new Random();
			load.add("i"+r.nextInt()+"_xy");
		}
		
		List<String>[] rs = Helper.getChunkedList(load, cs);
		for(List<String> v : rs){
			
			System.out.println(v.toString());
			
		}
		
		return rs.length;
	}

}
