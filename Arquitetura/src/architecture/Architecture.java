package architecture;

import components.Bus;
import components.Demux;
import components.Memory;
import components.Register;
import components.Ula;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Architecture {

  private final boolean simulation;
  private boolean halt;

  private Bus intbus;
  private Bus extbus;

  private Register PC;
  private Register IR;
  private Register RPG;
  private Register RPG1;
  private Register RPG2;
  private Register RPG3;
  private Register flags;
  private Demux demux;

  private Ula ula;

  private Memory statusMemory;
  private Memory memory;
  private int memorySize;

  private ArrayList<String> commandsList;
  private ArrayList<Register> registersList;

  
  private void componentsInstances() {
    
    extbus = new Bus();
    intbus = new Bus();

    
    PC = new Register("PC", intbus, intbus);
    IR = new Register("IR", extbus, intbus);
    RPG = new Register("RPG0", extbus, extbus);
    RPG1 = new Register("RPG1", extbus, extbus);
    RPG2 = new Register("RPG2", extbus, extbus);
    RPG3 = new Register("RPG3", extbus, extbus);

    flags = new Register(2, extbus);
    fillRegistersList();

 
    ula = new Ula(extbus, intbus);
    statusMemory = new Memory(2, extbus);

    memorySize = 128;
    memory = new Memory(memorySize, extbus);

    demux = new Demux();

    fillCommandsList();
  }

  /**
   * This method fills the registers list inserting into them all the registers we have.
   * IMPORTANT!
   * The first register to be inserted must be the default RPG
   */
  private void fillRegistersList() {
    registersList = new ArrayList<>();
    registersList.add(RPG); 
    registersList.add(RPG1);
    registersList.add(RPG2);
    registersList.add(RPG3);
    registersList.add(IR);
    registersList.add(PC);
    registersList.add(flags);
  }

  /**
   * Constructor that instanciates all components according the architecture diagram
   *
   * @param simulation Set the simulation mode
   */
  public Architecture(boolean simulation) {
    componentsInstances();
    this.simulation = simulation;
  }

  public Architecture() {
    this(false);
  }

  /*
   * Getters
   */
  public Bus getExtbus() {
    return extbus;
    
  }

  public Bus getIntbus() {
    return intbus;
  }

  public Register getIR() {
    return IR;
  }

  public Register getPC() {
    return PC;
  }

  public Register getRPG() {
    return RPG;
  }

  public Register getRPG1() {
    return RPG1;
  }

  public Register getRPG2() {
    return RPG2;
  }

  public Register getRPG3() {
    return RPG3;
  }

  public Register getFlags() {
    return flags;
  }

  public Demux getDemux() {
    return demux;
  }

  public Ula getUla() {
    return ula;
  }

  public Memory getStatusMemory() {
    return statusMemory;
  }

  public Memory getMemory() {
    return memory;
  }

  public ArrayList<Register> getRegistersList() {
    return registersList;
  }

  public ArrayList<String> getCommandsList() {
    return commandsList;
  }

  public int getMemorySize() {
    return memorySize;
  }

 
  protected void fillCommandsList() {
    commandsList = new ArrayList<>();
    	commandsList.add("addRegReg"); //0
		commandsList.add("addMemReg"); //1
		commandsList.add("addRegMem"); //2
		commandsList.add("addImmReg"); //3
		commandsList.add("subRegReg"); //4
		commandsList.add("subMemReg"); //5
		commandsList.add("subRegMem"); //6
		commandsList.add("subImmReg"); //7
		commandsList.add("imulMemReg"); //8 NAO FEITO
		commandsList.add("imulRegMem"); //9 NAO FEITO
		commandsList.add("imulRegReg"); //10 NAO FEITO
		commandsList.add("moveMemReg"); //11
		commandsList.add("moveRegMem"); //12
		commandsList.add("moveRegReg"); //13
		commandsList.add("moveImmReg"); //14
		commandsList.add("incReg"); //15
		commandsList.add("jmp"); //16
		commandsList.add("jn"); //17
		commandsList.add("jz"); //18
		commandsList.add("jeq"); //19
		commandsList.add("jneq"); //20
		commandsList.add("jgt"); //21
		commandsList.add("jlw"); //22
  }

  /**
   * This method is used after some ULA operations, setting the flags bits according the result.
   *
   * @param result is the result of the operation
   *               NOT TESTED!!!!!!!
   */
  public void setStatusFlags(int result) {
    flags.setBit(0, 0);
    flags.setBit(1, 0);
    if (result == 0) { // bit 0 in flags must be 1 in this case
      flags.setBit(0, 1);
    }
    if (result < 0) { // bit 1 in flags must be 1 in this case
      flags.setBit(1, 1);
    }
  }
  public void addRegReg() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	    // Lê o ID do primeiro registrador da memória
	    memory.read();
	    // Seleciona o primeiro registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Aguarda pelo valor do segundo registrador
	    ula.store(0);

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do segundo registrador na ULA
	    ula.store(1);

	    // Adiciona o valor do primeiro registrador ao valor do segundo registrador
	    ula.add();

	    // Move o valor do PC para o extbus
	    ula.internalStore(0);
	    ula.read(0);

	    // Escreve o valor da ULA no registrador selecionado
	    memory.read();
	    demux.setValue(extbus.get());
	    ula.read(1);
	    registersStore();

	    setStatusFlags(extbus.get()); // Define as flags conforme o resultado

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

	public void addMemReg() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o valor da memória
	    memory.read();
	    memory.read();

	    // Aguarda pelo valor do segundo registrador
	    ula.store(0);

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do segundo registrador na ULA
	    ula.store(1);

	    // Adiciona o valor do primeiro registrador ao valor do segundo registrador
	    ula.add();

	    // Move o valor do PC para o extbus
	    ula.internalStore(0);
	    ula.read(0);

	    // Escreve o valor da ULA no registrador selecionado
	    memory.read();
	    demux.setValue(extbus.get());
	    ula.read(1);
	    registersStore();

	    setStatusFlags(extbus.get()); // Define as flags conforme o resultado

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

	public void addRegMem() {
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	    memory.read();
	    demux.setValue(extbus.get());
	    registersRead();
	    ula.store(0);
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	    memory.read();
	    memory.store();
	    memory.read();
	    ula.store(1);
	    ula.add();
	    ula.read(1);
	    setStatusFlags(extbus.get()); 
	    memory.store();
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	}


	public void addImmReg() {
	    // Incrementa o PC para apontar para o valor imediato
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	  
	    // Lê o valor imediato da memória
	    memory.read();
	    ula.store(0);
	  
	    // Incrementa o PC para apontar para o ID do registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	  
	    // Lê o ID do registrador da memória
	    memory.read();
	    demux.setValue(extbus.get());
	  
	    // Seleciona o registrador e lê seu valor
	    registersRead();
	    ula.store(1);
	  
	    // Adiciona o valor imediato ao valor do registrador
	    ula.add();
	  
	    // Escreve o resultado de volta no registrador
	    ula.read(1);
	    demux.setValue(extbus.get());
	    registersStore();
	    setStatusFlags(extbus.get());
	  
	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

	public void subRegReg() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do primeiro registrador da memória
	    memory.read();

	    // Seleciona o primeiro registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Aguarda pelo valor do segundo registrador
	    ula.store(0);

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do segundo registrador na ULA
	    ula.store(1);

	    // Subtrai o valor do primeiro registrador do valor do segundo registrador
	    ula.sub();

	    // Move o valor do PC para o extbus
	    ula.internalStore(0);
	    ula.read(0);

	    // Escreve o valor da ULA no registrador selecionado
	    memory.read();
	    demux.setValue(extbus.get());
	    ula.read(1);
	    registersStore();
	    setStatusFlags(extbus.get()); // Define as flags conforme o resultado

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

	public void subMemReg() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do primeiro registrador da memória
	    memory.read();
	    memory.read();

	    // Aguarda pelo valor do segundo registrador
	    ula.store(0);

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do segundo registrador na ULA
	    ula.store(1);

	    // Subtrai o valor do primeiro registrador do valor do segundo registrador
	    ula.sub();

	    // Move o valor do PC para o extbus
	    ula.internalStore(0);
	    ula.read(0);

	    // Escreve o valor da ULA no registrador selecionado
	    memory.read();
	    demux.setValue(extbus.get());
	    ula.read(1);
	    registersStore();
	    setStatusFlags(extbus.get()); // Define as flags conforme o resultado

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}


 
	public void subRegMem() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	    
	    // Lê o valor da memória
	    memory.read();
	    demux.setValue(extbus.get());
	    registersRead();
	    ula.store(0);
	    
	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	    
	    // Lê o ID do segundo registrador da memória
	    memory.read();
	    memory.store();
	    memory.read();
	    ula.store(1);
	    
	    // Subtrai o valor do primeiro registrador do valor do segundo registrador
	    ula.sub();
	    
	    // Lê o resultado da ULA
	    ula.read(1);
	    setStatusFlags(extbus.get()); // Define as flags conforme o resultado
	    
	    // Armazena o resultado na memória
	    memory.store();
	    
	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	}

	public void subImmReg() {
	    // Incrementa o PC para apontar para o valor imediato
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	  
	    // Lê o valor imediato da memória
	    memory.read();
	    ula.store(0);
	  
	    // Incrementa o PC para apontar para o ID do registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();
	  
	    // Lê o ID do registrador da memória
	    memory.read();
	    demux.setValue(extbus.get());
	  
	    // Seleciona o registrador e lê seu valor
	    registersRead();
	    ula.store(1);
	  
	    // Subtrai o valor imediato do valor do registrador
	    ula.sub();
	  
	    // Escreve o resultado de volta no registrador
	    ula.read(1);
	    demux.setValue(extbus.get());
	    registersStore();
	    setStatusFlags(extbus.get());
	  
	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

 
  public void imulMemReg() {
    
}

 
  public void ImulRegMem() {
	  
	}
  
  public void imulRegReg() {
     
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();	

  }


  public void moveMemReg() {
	    // Incrementa o PC para apontar para a posição de memória
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    memory.read(); // Obtém o primeiro parâmetro
	    memory.read(); // Obtém o valor da posição de memória
	    ula.store(0); // Salva o valor do extbus na ULA

	    // Incrementa o PC para apontar para o ID do registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    memory.read(); // Obtém o ID do registrador

	    demux.setValue(extbus.get()); // Define o valor do demux como o ID do registrador
	    ula.read(0); // Move o valor da ULA para o extbus
	    registersStore(); // Escreve o valor do extbus no registrador selecionado

	    // Incrementa o PC para apontar novamente para a posição de memória
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

	public void moveRegMem() {
	    // Incrementa o PC para apontar para o registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do registrador da memória
	    memory.read();
	    ula.store(0); // armazena o ID do registrador na ULA

	    // Incrementa o PC para apontar para a posição de memória
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    memory.read(); // Obtém o valor da posição de memória
	    memory.store(); // Envia a posição de memória e espera pelo valor

	    // Move o valor da ULA para o extbus
	    ula.read(0);

	    // Escreve o valor do extbus na memória
	    demux.setValue(extbus.get());
	    registersRead();

	    // Escreve o valor do extbus na memória
	    memory.store();

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

	public void moveRegReg() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do primeiro registrador da memória
	    memory.read();
	    ula.store(0); // armazena o ID do registrador na ULA

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	    ula.read(0); // envia o valor do extbus de volta para o extbus

	    // Seleciona o primeiro registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do primeiro registrador na ULA
	    ula.store(0);

	    // Move o valor do PC para o extbus
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.read(1);

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador
	    demux.setValue(extbus.get());

	    // Move o valor do primeiro registrador que foi armazenado na ULA para o extbus
	    ula.read(0);

	    // Escreve o valor da ULA no registrador selecionado
	    registersStore();

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}


	void moveImmReg() {
	    // Incrementa o PC para apontar para o valor imediato
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o valor imediato da memória
	    memory.read();
	    ula.store(0); // armazena o valor imediato na ULA

	    // Incrementa o PC para apontar para o registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do registrador da memória
	    memory.read();

	    // Seleciona o registrador
	    demux.setValue(extbus.get());

	    // Move o valor imediato que foi armazenado na ULA para o extbus
	    ula.read(0);

	    // Escreve o valor da ULA no registrador selecionado
	    registersStore();

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

	public void incReg() {
	    // Incrementa o PC para apontar para o registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do registrador da memória
	    memory.read();

	    // Seleciona o registrador
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do registrador selecionado na ULA
	    ula.store(1);

	    // Incrementa o valor na ULA
	    ula.inc();

	    // Move o valor da ULA para o extbus
	    ula.read(1);
	    setStatusFlags(extbus.get()); // Define as flags de acordo com o resultado

	    // Escreve o valor da ULA no registrador selecionado
	    registersStore();

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    PC.internalStore();
	}

  
  public void jmp() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

 
  public void jn() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    statusMemory.storeIn0();

    extbus.put(flags.getBit(1));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);

    PC.internalStore();
  }

  public void jz() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    statusMemory.storeIn0();

    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);

    PC.internalStore();
  }

 

  public void jeq() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do primeiro registrador da memória
	    memory.read();

	    // Seleciona o primeiro registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Aguarda o valor do segundo registrador
	    ula.store(0);

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do segundo registrador na ULA
	    ula.store(1);

	    // Subtrai o valor do primeiro registrador do valor do segundo registrador
	    ula.sub();
	    ula.read(1);
	    setStatusFlags(extbus.get()); // Define as flags de acordo com o resultado

	    // Incrementa o PC para apontar para a posição de memória
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê a posição de memória da memória
	    memory.read();

	    // Armazena o status na memória
	    statusMemory.storeIn1();

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Armazena o status na memória
	    statusMemory.storeIn0();

	    // Coloca o bit 0 no extbus para leitura
	    extbus.put(flags.getBit(0));
	    statusMemory.read();

	    ula.store(0);
	    ula.internalRead(0);
	    PC.internalStore();
	}

	public void jgt() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do primeiro registrador da memória
	    memory.read();

	    // Seleciona o primeiro registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Aguarda o valor do segundo registrador
	    ula.store(0);

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do segundo registrador na ULA
	    ula.store(1);

	    // Subtrai o valor do primeiro registrador do valor do segundo registrador
	    ula.sub();
	    ula.read(1);
	    setStatusFlags(extbus.get()); // Define as flags de acordo com o resultado

	    // Incrementa o PC para apontar para a posição de memória
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê a posição de memória da memória
	    memory.read();

	    // Armazena o status na memória
	    statusMemory.storeIn1();

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Armazena o status na memória
	    statusMemory.storeIn0();

	    // Verifica se é > e não >=
	    extbus.put(flags.getBit(1));
	    ula.store(0);
	    extbus.put(flags.getBit(0));
	    ula.store(1);
	    ula.sub(); // Se o resultado for 0, o primeiro registrador é maior que o segundo e não igual
	    ula.read(1);
	    setStatusFlags(extbus.get());
	    extbus.put(flags.getBit(0));
	    statusMemory.read();

	    ula.store(0);
	    ula.internalRead(0);
	    PC.internalStore();
	}

  
	public void jlw() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do primeiro registrador da memória
	    memory.read();

	    // Seleciona o primeiro registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Aguarda o valor do segundo registrador
	    ula.store(0);

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Salva o valor do segundo registrador na ULA
	    ula.store(1);

	    // Subtrai o valor do primeiro registrador do valor do segundo registrador
	    ula.sub();
	    ula.read(1);
	    setStatusFlags(extbus.get()); // Define as flags de acordo com o resultado

	    // Incrementa o PC para apontar para a posição de memória
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê a posição de memória da memória
	    memory.read();

	    // Armazena o status na memória
	    statusMemory.storeIn1();

	    // Incrementa o PC para apontar para o próximo comando
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Armazena o status na memória
	    statusMemory.storeIn0();

	    // Verifica se é > e não >=
	    extbus.put(flags.getBit(1));
	    ula.store(0);
	    extbus.put(flags.getBit(0));
	    ula.store(1);
	    ula.sub(); // Se o resultado não for negativo e não for zero, o primeiro registrador é menor que o segundo
	    ula.read(1);
	    setStatusFlags(extbus.get());

	    extbus.put(flags.getBit(1));
	    ula.store(0);
	    extbus.put(flags.getBit(0));
	    ula.store(1);
	    ula.sub(); // Se o resultado for 0, o primeiro registrador é maior que o segundo e não igual
	    ula.read(1);
	    setStatusFlags(extbus.get());
	    extbus.put(flags.getBit(0));
	    statusMemory.read();

	    ula.store(0);
	    ula.internalRead(0);
	    PC.internalStore();
	}

	public void jneq() {
	    // Incrementa o PC para apontar para o primeiro registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do primeiro registrador da memória
	    memory.read();

	    // Seleciona o primeiro registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Armazena o valor do primeiro registrador na ULA
	    ula.store(0);

	    // Incrementa o PC para apontar para o segundo registrador
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê o ID do segundo registrador da memória
	    memory.read();

	    // Seleciona o segundo registrador e lê seu valor
	    demux.setValue(extbus.get());
	    registersRead();

	    // Armazena o valor do segundo registrador na ULA
	    ula.store(1);

	    // Subtrai o valor do primeiro registrador do valor do segundo registrador
	    ula.sub();
	    ula.read(1);
	    setStatusFlags(extbus.get()); // Define as flags de acordo com o resultado

	    // Incrementa o PC para apontar para a posição de memória
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Lê a posição de memória da memória
	    memory.read();

	    // Armazena a posição de memória para saltar em statusMemory
	    statusMemory.storeIn1();

	    // Incrementa o PC para apontar para a próxima instrução
	    PC.internalRead();
	    ula.internalStore(1);
	    ula.inc();
	    ula.internalRead(1);
	    ula.read(1);
	    PC.internalStore();

	    // Armazena a posição de memória para saltar em statusMemory
	    statusMemory.storeIn0();

	    // Verifica se o resultado da subtração não é zero (ou seja, os registradores não são iguais)
	    extbus.put(flags.getBit(0));
	    statusMemory.read();

	    ula.store(0);
	    ula.internalRead(0);
	    PC.internalStore();
	}



  /**
   * This method performs an (external) read from a register into the register list.
   * The register id must be in the demux bus
   */
  private void registersRead() {
    registersList.get(demux.getValue()).read();
  }

  /**
   * This method performs an (internal) read from a register into the register list.
   * The register id must be in the demux bus
   */
  private void registersInternalRead() {
    registersList.get(demux.getValue()).internalRead();
  }

  /**
   * This method performs an (external) store toa register into the register list.
   * The register id must be in the demux bus
   */
  private void registersStore() {
    registersList.get(demux.getValue()).store();
  }

  /**
   * This method performs an (internal) store toa register into the register list.
   * The register id must be in the demux bus
   */
  private void registersInternalStore() {
    registersList.get(demux.getValue()).internalStore();
  }

  /**
   * This method reads an entire file in machine code and
   * stores it into the memory
   * NOT TESTED
   *
   * @param filename the name of the file to be read
   * @throws IOException if the file is not found
   */
  public void readExec(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename + ".dxf"));
    String linha;
    int i = 0;
    while ((linha = br.readLine()) != null) {
      extbus.put(i);
      memory.store();
      extbus.put(Integer.parseInt(linha));
      memory.store();
      i++;
    }
    br.close();
  }

  /**
   * This method executes a program that is stored in the memory
   */
  public void controlUnitEexec() {
    halt = false;
    while (!halt) {
      fetch();
      decodeExecute();
    }
  }

  /**
   * This method implements the decoding process,
   * that is to find the correct operation do be executed
   * according the command.
   * And the executing process, that is the execution itself of the command
   */
  private void decodeExecute() {
    IR.internalRead(); // the instruction is in the internal bus
    int command = intbus.get();
    simulationDecodeExecuteBefore(command);

    switch (command) {
      case 0:
        addRegReg();
        break;
      case 1:
        addMemReg();
        break;
      case 2:
        addRegMem();
        break;
      case 3:
        addImmReg();
        break;
      case 4:
        subRegReg();
        break;
      case 5:
        subMemReg();
        break;
      case 6:
        subRegMem();
        break;
      case 7:
        subImmReg();
        break;
      case 8:
        imulMemReg();
        break;
      case 9:
        break;
      case 10:
        imulRegReg();
        break;
      case 11:
        moveMemReg();
        break;
      case 12:
        moveRegMem();
        break;
      case 13:
        moveRegReg();
        break;
      case 14:
        moveImmReg();
        break;
      case 15:
        incReg();
        break;
      case 16:
        jmp();
        break;
      case 17:
        jn();
        break;
      case 18:
        jz();
        break;
      case 19:
        jeq();
        break;
      case 20:
        jneq();
        break;
      case 21:
        jgt();
        break;
      case 22:
        jlw();
        break;
      default:
        halt = true;
        break;
      }

    if (simulation) {
      simulationDecodeExecuteAfter();
    }
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED
   *
   * @param command the command to be executed
   */
  private void simulationDecodeExecuteBefore(int command) {
    System.out.println("----------BEFORE Decode and Execute phases--------------");
    String instruction;
    int parameter = 0;
    for (Register r : registersList) {
      System.out.println(r.getRegisterName() + ": " + r.getData());
    }
    if (command != -1) {
      instruction = commandsList.get(command);
    } else {
      instruction = "END";
    }

    int operands = getOperandSize(instruction);
    System.out.print("Instruction: " + instruction);
    for (int i = 0; i < operands; i++) {
      parameter = memory.getDataList()[PC.getData() + i + 1];
      System.out.print(" " + parameter);
    }
    System.out.println();

    if ("read".equals(instruction)) {
      System.out.println("memory[" + parameter + "]=" + memory.getDataList()[parameter]);
    }
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED
   */
  private void simulationDecodeExecuteAfter() {
    String instruction;
    System.out.println("-----------AFTER Decode and Execute phases--------------");
    System.out.println("Internal Bus 1: " + intbus.get());
    System.out.println("Internal Bus 2: " + intbus.get());
    System.out.println("External Bus 1: " + extbus.get());
    for (Register r : registersList) {
      System.out.println(r.getRegisterName() + ": " + r.getData());
    }
    Scanner entrada = new Scanner(System.in);
    System.out.println("Press <Enter>");
    String mensagem = entrada.nextLine();
  }

  /**
   * This method uses PC to find, in the memory,
   * the command code that must be executed.
   * This command must be stored in IR
   * NOT TESTED!
   */
  private void fetch() {
    PC.internalRead();
    ula.internalStore(0);
    ula.read(0);
    memory.read();
    IR.store();
    simulationFetch();
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED!!!!!!!!!
   */
  private void simulationFetch() {
    if (simulation) {
      System.out.println("-------Fetch Phase------");
      System.out.println("PC: " + PC.getData());
      System.out.println("IR: " + IR.getData());
    }
  }

  /**
   * This method is used to show in a correct way the operands (if there is any) of instruction,
   * when in simulation mode
   * NOT TESTED!!!!!
   *
   * @param instruction the instruction to be executed
   * @return the number of operands
   */
  private int getOperandSize(String instruction) {
    if (instruction.equals("END")) {
      return 0;
    }

    int operands = 0;
    for (int i = 0; i < instruction.length(); i++) {
      // check if the character is a capital letter
      if (Character.isUpperCase(instruction.charAt(i))) {
        operands++;
      }
    }
    return operands;
  }

  public static void main(String[] args) throws IOException {
    Architecture arch = new Architecture(true);
    arch.readExec("program");
    arch.controlUnitEexec();
  }
}