package application.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class SearchUtil {
	/*
	 * 广度优先搜索文件或文件夹
	 * 
	 * @param path 要搜索的目录
	 * 
	 * @param regex 搜索的通配符
	 * 
	 * @param isDisplyDir 是否在搜索结果中显示文件夹
	 * 
	 * @param isDisplayFile 是否在搜索结果中显示文件
	 */
	private static void bfsSearchFile(String path, String regex, boolean isDisplyDir, boolean isDisplayFile) {
		if (!(isDisplayFile || isDisplyDir)) {
			throw new IllegalArgumentException("isDisplyDir和isDisplayFile中至少要有一个为true");
		}
		Queue<File> queue = new LinkedList<>();
		File[] fs = new File(path).listFiles();
		// 遍历第一层
		for (File f : fs) {
			// 把第一层文件夹加入队列
			if (f.isDirectory()) {
				queue.offer(f);
			} else {
				if (f.getName().matches(regex) && isDisplayFile) {
					System.out.println(f.getName());
				}
			}
		}
		// 逐层搜索下去
		while (!queue.isEmpty()) {
			File fileTemp = queue.poll();// 从队列头取一个元素
			if (isDisplyDir) {
				if (fileTemp.getName().matches(regex)) {
					System.out.println(fileTemp.getAbsolutePath());
				}
			}

			File[] fileListTemp = fileTemp.listFiles();
			if (fileListTemp == null)
				continue;// 遇到无法访问的文件夹跳过
			for (File f : fileListTemp) {
				if (f.isDirectory()) {
					queue.offer(f);//// 从队列尾插入一个元素
				} else {
					if (f.getName().matches(regex) && isDisplayFile) {
						System.out.println(f.getName());
					}
				}
			}

		}
	}
	public static void main(String[] args) {
		File file = new File("C://");
		bfsSearchFile("E:\\工电供资料\\document\\6.客户资料\\工务\\工务检测数据\\2019年第三季度综合检测车联检\\北同蒲",".iic",true,false);
	}
}
