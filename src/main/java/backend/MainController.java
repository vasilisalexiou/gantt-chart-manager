package backend;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import dom2app.SimpleTableModel;

public class MainController implements IMainController {
	
	String[] COLUMN_NAMES = {"TaskId" , "TaskText", "MamaId","Start" , "End" , "Cost" };
	List<String[]> data=new ArrayList<String[]>();
	String fileName;
	@Override
	public SimpleTableModel load(String fileName, String delimiter) {
		this.fileName=fileName;
		List<String[]> leader=new ArrayList<String[]>();
		List<String[]> followers=new ArrayList<String[]>();
		Scanner sc=null;
		try {
			sc=new Scanner(new File(fileName));
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}
		String[] line;
		while(sc.hasNextLine())
		{
			line=sc.nextLine().split(delimiter);
			if (!line[0].equals("TaskId")) {
				if(line.length<4) {
					leader.add(line);
				}else {
					followers.add(line);
				}
			}
		}
		for(int i=0;i<leader.size();i++) {
			int start=100000;
			int end=0;
			int cost=0;
			String mamaID=leader.get(i)[0];
			for (int j=0;j<followers.size();j++){
				if(followers.get(j)[2].equals(mamaID)) {
					if(Integer.parseInt(followers.get(j)[3])<start) {
						start=Integer.parseInt(followers.get(j)[3]);
					}
					if(Integer.parseInt(followers.get(j)[3])>end) {
						end=Integer.parseInt(followers.get(j)[4]);
					}
					cost+=Integer.parseInt(followers.get(j)[5]);
				}
			}
			String [] leaderHead= {leader.get(i)[0],leader.get(i)[1],leader.get(i)[2],String.valueOf(start),String.valueOf(end),String.valueOf(cost)};
			leader.set(i, leaderHead);
		}
		Comparator<String[]> comp=new Comparator<String[]>() {
			@Override
			public int compare(String[] o1,String[]o2){
				if(Integer.valueOf(o1[3])<Integer.valueOf(o2[3])||(Integer.valueOf(o1[3])==Integer.valueOf(o2[3])&&((Integer.valueOf(o1[2])==0)||((Integer.valueOf(o2[2])!=0)&&Integer.valueOf(o1[4])<Integer.valueOf(o2[4]))))) {
					return -10;
				}
				if(Integer.valueOf(o1[3])>Integer.valueOf(o2[3])||(Integer.valueOf(o1[3])==Integer.valueOf(o2[3])&&((Integer.valueOf(o2[2])==0)||((Integer.valueOf(o2[2])!=0)&&Integer.valueOf(o1[4])>Integer.valueOf(o2[4]))))) {
					return 10;
				}
				return 0;
			}
		};
		leader.sort(comp);
		followers.sort(comp);
		for(int i=0;i<leader.size();i++) {
			data.add(leader.get(i));
			for(int j=0;j<followers.size();j++) {
				if(followers.get(j)[2].equals(leader.get(i)[0])) {
					data.add(followers.get(j));
				}
			}
		}
		return new SimpleTableModel("All Tasks",this.fileName,this.COLUMN_NAMES,this.data);
	}

	@Override
	public SimpleTableModel getTasksByPrefix(String prefix) {
		String title="Filtered by prefix: "+prefix;
		List<String[]> filtered=new ArrayList<String[]>();
		for(int i=0;i<data.size();i++) {
			if(data.get(i)[1].startsWith(prefix)) {
				filtered.add(data.get(i));
			}
		}
		return new SimpleTableModel(title,this.fileName,this.COLUMN_NAMES,filtered);
	}

	@Override
	public SimpleTableModel getTaskById(int id) {
		String title="Filtered by TaskID: "+id;
		List<String[]> filtered=new ArrayList<String[]>();
		for(int i=0;i<data.size();i++) {
			if(Integer.valueOf(data.get(i)[0])==id) {
				filtered.add(data.get(i));
			}
		}
		return new SimpleTableModel(title,this.fileName,this.COLUMN_NAMES,filtered);
	}

	@Override
	public SimpleTableModel getTopLevelTasks() {
		String title="Top Level Tasks";
		List<String[]> filtered=new ArrayList<String[]>();
		for(int i=0;i<data.size();i++) {
			if(Integer.valueOf(data.get(i)[2])==0) {
				filtered.add(data.get(i));
			}
		}
		return new SimpleTableModel(title,this.fileName,this.COLUMN_NAMES,filtered);
	}
	@Override
	public int createReport(String path, ReportType type) {
		if(type==ReportType.TEXT) {
			try {
				 FileOutputStream fos=new FileOutputStream(path);
				 DataOutputStream outStream=new DataOutputStream(new BufferedOutputStream(fos));
					String header="TaskId\tTaskText\tMamaId\tStart\tEnd\tCost\t";
					outStream.writeUTF(header);
					for(int i=0;i<this.data.size();i++) {
						for(int j=0;j<this.data.get(i).length;j++) {
							outStream.writeUTF(this.data.get(i)[j]);
							outStream.writeUTF("\t");
						}
						outStream.writeUTF("\n");
					}
					outStream.close();
			}catch(IOException e) {
				System.out.println("Error creating a TXT file");
			}
		}
		if(type==ReportType.HTML) {
			try {
				FileOutputStream fos=new FileOutputStream(path+".html");
				DataOutputStream outStream=new DataOutputStream(new BufferedOutputStream(fos));
				outStream.writeUTF("<!doctype html>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content\"text/html; charset=windows-1253\"\n");
				outStream.writeUTF("<title>"+this.fileName+"</title>\n</head>\n<body>\n\n<table>\n<tr>\n");
				outStream.writeUTF("<td>TaskId</td> <td>TaskText</td> <td>MamaId</td> <td>Start</td> <td>End</td> <td>Cost</td> </tr\n");
				for(int i=0;i<this.data.size();i++) {
					outStream.writeUTF("<tr>\n");
					for(int j=0;j<this.data.get(i).length;j++) {
						outStream.writeUTF("<td>"+this.data.get(i)[j]+"</td>");
					}
					outStream.writeUTF("</tr>\n");
				}
				outStream.close();
			}catch(IOException e) {
				System.out.println("Error creating a HTML file");
			}
		}
		if(type==ReportType.MD) {
			try {
				FileOutputStream fos=new FileOutputStream(path+".md");
				DataOutputStream outStream=new DataOutputStream(new BufferedOutputStream(fos));
				String header="TaskId TaskText MamaId Start End Cost";
				outStream.writeUTF("<p>");
				outStream.writeUTF("<em>"+header+"</em><br>\n");
				for(int i=0;i<this.data.size();i++) {
					if(Integer.parseInt(this.data.get(i)[2])==0) {
						outStream.writeUTF("<strong>");
					}
					for(int j=0;j<this.data.get(i).length;j++) {
						outStream.writeUTF(this.data.get(i)[j]);
						outStream.writeUTF(" ");
					}
					if(Integer.parseInt(this.data.get(i)[2])==0) {
						outStream.writeUTF("</strong>");
					}
					outStream.writeUTF("<br>\n");
				}
				outStream.close();
			}catch(IOException e) {
				System.out.println("Error creating a Markdown file");
			}
		}
		return 0;
	}
}