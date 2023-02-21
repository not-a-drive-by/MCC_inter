/*
* 任务要能够在仿真之前先产生并写入文件
* 所以这里的main要能够独立运行 元素很多static
* */

package edu.boun.edgecloudsim.task_generator;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Variable;
import org.w3c.dom.Document;

import java.io.*;
import java.util.*;

public class TaskGeneratorModel {

    //配置文件数据来源
    static SimSettings SS = SimSettings.getInstance();

    //文件路径信息
    private static String mobileDevicesFile = "ECSim/EdgeCloudSim/scripts/sample_app1/config/mobile_devices.xml";

    private static ArrayList<List<Task>> taskList; //表示是一个关于 Task类的列表

    private static Document edgeDevicesDoc = null;
    private static int _MobileDeviceNum;
    public static ArrayList<DeviceTaskStatic> _mobileDeviceStatic = new ArrayList<>();

    public static void main(String[] args) throws IOException,ClassNotFoundException {
        SS.init(mobileDevicesFile);
        _MobileDeviceNum = SS.MobileDeviceNum;
        _mobileDeviceStatic = SS.mobileDeviceStatic;
        taskList = produceTask(_mobileDeviceStatic);
//        System.out.println(taskList);
        //写入任务
        FileOutputStream fos=new FileOutputStream("TaskInformation.txt");
        ObjectOutputStream os=new ObjectOutputStream(fos);
        try
        {
            for(int i=0;i<taskList.size();i++)
            {
                os.writeObject(taskList.get(i));
            }
            os.close();
        }catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
        //读出任务
        List<Task> t = null;
        FileInputStream fi=new FileInputStream("TaskInformation.txt");
        ObjectInputStream si=new ObjectInputStream(fi);
        try
        {
            taskList.clear();
            for(int i=0;i<_MobileDeviceNum;i++)
            {
                t=(List<Task>)si.readObject();
                taskList.add(t);
            }
            si.close();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println(taskList);


    }


    private static ArrayList<List<Task>> produceTask(ArrayList<DeviceTaskStatic> MobileDeviceStatic){
        ArrayList<List<Task>> taskList = new ArrayList<List<Task>>();
        int taskID = 1;
        Random r = new Random();

        Variable.updatePoissonGenerator(2);

        for(int i=0; i<MobileDeviceStatic.size(); i++){
            DeviceTaskStatic deviceTaskStatic = MobileDeviceStatic.get(i);
//            System.out.println(deviceTaskStatic);
            int taskNum = deviceTaskStatic.taskNum;//一个设备的任务总数

            List<Task> tList = new ArrayList<Task>();//每个设备的任务集合
            for(int k=0; k< (int) taskNum*deviceTaskStatic.type1Ratio; k++){
//                tList.add(new Task(deviceTaskStatic.meanLen1, 32, 2, 1690, taskID++, 0.1));
                tList.add(new Task(1+r.nextInt(2), 32, 2, 1690, taskID++, 0.1));
            }
            Variable.updateParetoGenerator( 0.4, deviceTaskStatic.meanLen2 );
            for(int k=0; k< (int) taskNum*deviceTaskStatic.type2Ratio; k++){
                tList.add(new Task(1+r.nextInt(3), 30, 2, 420, taskID++,0.2));
            }
            Variable.updateParetoGenerator( 0.4, deviceTaskStatic.meanLen3 );
            for(int k=0; k< (int) taskNum*deviceTaskStatic.type3Ratio; k++){
                tList.add(new Task(1+r.nextInt(4), 7, 2, 1690, taskID++,0.3));
            }

            //打乱任务 设置到达时间
            int sum = 0;
            Collections.shuffle(tList);
            for( Task task : tList){
                task.setArrivalTime(sum);
                sum += Variable.Pareto_Distribution();
            }

//            Collections.sort(tList, new TaskComparatorByTime());//将任务按达到时间排序

            taskList.add(tList);

        }
        return taskList;

    }

    //根据到达时间升序排序任务，需要导入包java.util.Comparator
    public static class TaskComparatorByTime implements Comparator<Task>
    {
        public int compare(Task tl1, Task tl2)
        {
            return (int) (tl1.arrivalTime - tl2.arrivalTime);
        }
    }


}
