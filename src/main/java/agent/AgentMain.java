package agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;
import java.util.StringJoiner;

public class AgentMain {
    public static void main(String[] args) throws Exception {
        String option=args[0];
        List<VirtualMachineDescriptor> list =
                VirtualMachine.list();
        if (option.equals("list")){
            for (VirtualMachineDescriptor vmd : list) {
                System.out.println(vmd.displayName());
            }
        }else if(option.equals("attach")){
            String jProcessName = args[1];
            String agentPath = args[2];

            for (VirtualMachineDescriptor vmd : list) {
                if(vmd.displayName().endsWith(jProcessName)){
                    VirtualMachine virtualMachine = VirtualMachine.attach(vmd.id());
                    StringJoiner stringJoiner = new StringJoiner(" ");
                    for (int i = 3; i < args.length; i++) {
                        stringJoiner.add(args[i]);
                    }
                   virtualMachine.loadAgent(agentPath,stringJoiner.toString());
                    System.out.println("loadAgent success");
                    virtualMachine.detach();
                }
            }
        }
    }
}
