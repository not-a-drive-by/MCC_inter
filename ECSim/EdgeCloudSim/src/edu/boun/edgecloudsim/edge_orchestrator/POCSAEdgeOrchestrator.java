package edu.boun.edgecloudsim.edge_orchestrator;

import edu.boun.edgecloudsim.edge_server.EdgeDataCenter;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.network.Channel;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.task_generator.Task;

import java.util.List;

public class POCSAEdgeOrchestrator extends EdgeOrchestrator{

    public POCSAEdgeOrchestrator( EdgeServerManager edgeServerManager ){
        this.EdgeServers = edgeServerManager.getEdgeServersList();
    }

    @Override
    public void Matching(NetworkModel networkModel){

        for ( Task task : preMatchTasks ){
            EdgeDataCenter minDelaySerer = null;
            EdgeDataCenter backupServer = null;
//            System.out.println("任务id是"+task.taskID);
            List<EdgeDataCenter> acceptableServers = task.getPreferenceList();
            if( acceptableServers.size()!=0 ){
                double min_dis = Double.POSITIVE_INFINITY;
                double min_delay = Double.POSITIVE_INFINITY;
                for( EdgeDataCenter server : acceptableServers ){
                    Channel cha = networkModel.serachChannelByDeviceandServer(
                            task.getMobileDevice().getMobileID(), server.getId() );
//                    System.out.print("距离"+cha.distance);
                    //找距离最小的
                    if( min_dis > cha.distance ){
                        min_dis = cha.distance;
                        backupServer = server;
                    }
                    //找对应队列空闲的虚拟机里delay最小的
                    if( server.getActiveVM().get(task.getType()-1).getProcessingTask() == null ){
                        double latency = task.dataSize / cha.ratio;
                        if( latency < min_delay ) {
                            min_delay = latency;
                            minDelaySerer = server;
                        }
                    }
                }
//                System.out.print("最后选择了"+min_dis);

            }
            task.setTargetServer( minDelaySerer==null ? backupServer : minDelaySerer );

        }

    }
}
