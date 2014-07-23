import exception.MyException;
import parser.*;
import syntaxtree.Goal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by ek on 7/2/14.
 */
public class Driver {
	private static FileInputStream stream = null;
	private static KangaParser parser;
	private static Goal tree;
	private static String target,mips;
	private static int dotIndex;
	private static PrintWriter out;

//	kanga/BinaryTree.kg
//	kanga/BubbleSort.kg
//	kanga/Factorial.kg
//	kanga/LinearSearch.kg
//	kanga/LinkedList.kg
//	kanga/MoreThan4.kg
//	kanga/QuickSort.kg
//	kanga/TreeVisitor.kg

	public static void main(String[] args) {
		for(String arg : args){
			try {
				System.out.println("Trying \'" + arg + "\' ...");
				if((dotIndex = arg.lastIndexOf(".kg")) == -1)
					throw new MyException("invalid file type, \'.kg\' expected.");

				target = arg.substring(0, dotIndex)+".mips";
				System.out.println("Output set to \'"+target+"\'.");
				stream = new FileInputStream(arg);
				parser = new KangaParser(stream);
				tree = parser.Goal();
				mips = tree.accept(new kanga2asm());
		//		System.out.println(mips);
				out = new PrintWriter(target);
				out.print(mips);
				out.flush();
				out.close();

			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
			} catch (ParseException e) {
				System.err.println(e.getMessage());
			} catch (MyException e) {
				System.err.println(e.getMessage());
				//e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(stream != null)
						stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
