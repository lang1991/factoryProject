package AgentsTests;

import static org.junit.Assert.*;
import helper.TestHelper;

import mocks.MockConveyorSystemController;
import mocks.MockGuiConveyorIn;
import mocks.MockGuiConveyorOut;
import mocks.MockKitRobot;

import org.junit.Before;
import org.junit.Test;

import NonAgent.EnteringConveyor;
import NonAgent.ExitingConveyor;
import NonAgent.Kit;
import NonAgent.Kit.KitState;
import agents.ConveyorSystemAgent;

public class ConveyorSystemAgentTest extends BasicTest
{
	ConveyorSystemAgent conveyorSystemAgent;
	EnteringConveyor enteringConveyor;
	ExitingConveyor exitingConveyor;
	private MockKitRobot kitRobot;
	MockGuiConveyorIn inGui;
	MockGuiConveyorOut outGui;
	MockConveyorSystemController controller;
	
	@Before
	public void setUp()
	{
		enteringConveyor = new EnteringConveyor();
		exitingConveyor = new ExitingConveyor();
		kitRobot = new MockKitRobot("KitRobot");
		conveyorSystemAgent = new ConveyorSystemAgent(enteringConveyor, exitingConveyor);
		conveyorSystemAgent.setKitRobot(kitRobot);
		inGui = new MockGuiConveyorIn(conveyorSystemAgent);
		outGui = new MockGuiConveyorOut(conveyorSystemAgent);
		enteringConveyor.setGui(inGui);
		exitingConveyor.setGui(outGui);
		controller = new MockConveyorSystemController(conveyorSystemAgent);
		conveyorSystemAgent.setController(controller);
	}
	
	@Test
	public void testMsgINeedEmptyKit()
	{
		assertNull(conveyorSystemAgent.getCurrentEnteringKit());
		
		conveyorSystemAgent.msgINeedEmptyKit("config");
		
		assertNotNull(conveyorSystemAgent.getCurrentEnteringKit());
		assertEquals("config", conveyorSystemAgent.getCurrentEnteringKit().getConfig());
		assertEquals(1, conveyorSystemAgent.getEnteringKits().size());
	}

	@Test
	public void testMsgKitPickedUp()
	{
		String config = "config";
		conveyorSystemAgent.msgINeedEmptyKit(config);
		
		Kit kit = conveyorSystemAgent.getCurrentEnteringKit();
		
		conveyorSystemAgent.msgKitPickedUp(kit);
		
		assertNull(conveyorSystemAgent.getCurrentEnteringKit());
		
		conveyorSystemAgent.msgINeedEmptyKit(config);
		conveyorSystemAgent.msgINeedEmptyKit(config);
		
		Kit kit2 = conveyorSystemAgent.getCurrentEnteringKit();
		Kit kit3 = conveyorSystemAgent.getEnteringKits().get(1);
		
		conveyorSystemAgent.msgKitPickedUp(kit2);
		
		assertFalse(conveyorSystemAgent.getEnteringKits().contains(kit2));
		assertEquals(1, conveyorSystemAgent.getEnteringKits().size());
		assertNotNull(conveyorSystemAgent.getCurrentEnteringKit());
		assertEquals(kit3, conveyorSystemAgent.getCurrentEnteringKit());
		
	}
	
	@Test
	public void testMsgKitDone()
	{
		Kit kit = new Kit();
		Kit kit2 = new Kit();
		
		conveyorSystemAgent.msgKitDone(kit);
		
		int index = conveyorSystemAgent.getExitingKits().indexOf(kit);
		
		assertEquals(kit, conveyorSystemAgent.getExitingKits().get(index));		
		assertEquals(1, conveyorSystemAgent.getExitingKits().size());
		
		conveyorSystemAgent.msgKitDone(kit2);
		
		index = conveyorSystemAgent.getExitingKits().indexOf(kit2);
		assertEquals(kit2, conveyorSystemAgent.getExitingKits().get(index));
		assertEquals(2, conveyorSystemAgent.getExitingKits().size());
		
	}
	
	@Test
	public void testMsgKitOutOfCell()
	{
		Kit kit = new Kit();
		Kit kit2 = new Kit();
		
		conveyorSystemAgent.msgKitDone(kit);
		assertTrue(conveyorSystemAgent.getExitingKits().contains(kit));
		
		conveyorSystemAgent.msgKitOutOfCell(kit);
		
		assertFalse(conveyorSystemAgent.getExitingKits().contains(kit));
		
		conveyorSystemAgent.msgKitDone(kit2);
		
		conveyorSystemAgent.msgKitOutOfCell(kit2);
		
		
	}
	
	@Test
	public void testMoveEmptyKitForRobotToPickUp()
	{
		conveyorSystemAgent.msgINeedEmptyKit("config");
		
		assertNotNull(conveyorSystemAgent.getCurrentEnteringKit());
		assertEquals(1, conveyorSystemAgent.getEnteringKits().size());
		
		Kit kit = conveyorSystemAgent.getCurrentEnteringKit();
		
		TestHelper.INSTANCE.callPrivateMethodWithArguments("agents.ConveyorSystemAgent", "moveEmptyKitForRobotToPickUp", conveyorSystemAgent, kit);
		
		assertTrue(controller.log.containsString("doAnim"));
		assertTrue(controller.log.containsString(kit.toString()));
		assertTrue(kitRobot.log.containsString("msgHereIsEmptyKit"));
		assertTrue(kitRobot.log.containsString(kit.toString()));
		
		controller.log.clear();
		kitRobot.log.clear();
		
		conveyorSystemAgent.msgINeedEmptyKit("config");
		
		TestHelper.INSTANCE.callPrivateMethodWithArguments("agents.ConveyorSystemAgent", "moveEmptyKitForRobotToPickUp", conveyorSystemAgent, kit);
		
		assertFalse(controller.log.containsString("doAnim"));
		assertFalse(kitRobot.log.containsString("msgHereIsEmptyKit"));
		assertEquals(2, conveyorSystemAgent.getEnteringKits().size());
		assertTrue(conveyorSystemAgent.getEnteringKits().get(0).equals(kit));
		assertFalse(conveyorSystemAgent.getEnteringKits().get(1).equals(kit));
		
		Kit kit2 = conveyorSystemAgent.getEnteringKits().get(1);
		
		conveyorSystemAgent.msgKitPickedUp(kit);
		
		assertNotNull(conveyorSystemAgent.getCurrentEnteringKit());
		assertEquals(kit2, conveyorSystemAgent.getCurrentEnteringKit());
		assertEquals(1, conveyorSystemAgent.getEnteringKits().size());
		
		TestHelper.INSTANCE.callPrivateMethodWithArguments("agents.ConveyorSystemAgent", "moveEmptyKitForRobotToPickUp", conveyorSystemAgent, kit2);
		
		assertTrue(controller.log.containsString("doAnim"));
		assertTrue(controller.log.containsString(kit2.toString()));
		assertTrue(kitRobot.log.containsString("msgHereIsEmptyKit"));
		assertTrue(kitRobot.log.containsString(kit2.toString()));
	
	}
	
	@Test
	public void testPickAndExecuteAnAction()
	{
		conveyorSystemAgent.msgINeedEmptyKit("config");
		
		assertNotNull(conveyorSystemAgent.getCurrentEnteringKit());
		assertEquals(1, conveyorSystemAgent.getEnteringKits().size());
		
		Kit kit = conveyorSystemAgent.getCurrentEnteringKit();
		
		TestHelper.INSTANCE.callPrivateMethod("agents.ConveyorSystemAgent", "pickAndExecuteAnAction", conveyorSystemAgent);
		
		assertTrue(controller.log.containsString("doAnim"));
		assertTrue(controller.log.containsString(kit.toString()));
		assertTrue(kitRobot.log.containsString("msgHereIsEmptyKit"));
		assertTrue(kitRobot.log.containsString(kit.toString()));
		
		conveyorSystemAgent.msgKitPickedUp(kit);
		conveyorSystemAgent.msgKitDone(kit);
		
		assertEquals(1, conveyorSystemAgent.getExitingKits().size());
		assertEquals(kit, conveyorSystemAgent.getExitingKits().get(0));
				
		TestHelper.INSTANCE.callPrivateMethod("agents.ConveyorSystemAgent", "pickAndExecuteAnAction", conveyorSystemAgent);
		
		conveyorSystemAgent.msgKitOutOfCell(kit);
		
		assertEquals(0, conveyorSystemAgent.getExitingKits().size());
		assertEquals(0, conveyorSystemAgent.getEnteringKits().size());		
		
	}
	
	@Test
	public void testMultipleKits()
	{
		conveyorSystemAgent.msgINeedEmptyKit("config");
		conveyorSystemAgent.msgINeedEmptyKit("config");
		Kit kit = conveyorSystemAgent.getEnteringKits().get(0);
		Kit kit2 = conveyorSystemAgent.getEnteringKits().get(1);
		assertNotSame(kit, kit2);
		assertEquals(2, conveyorSystemAgent.getEnteringKits().size());
		
		TestHelper.INSTANCE.callPrivateMethod("agents.ConveyorSystemAgent", "pickAndExecuteAnAction", conveyorSystemAgent);
		
		assertEquals(KitState.MOVING_OUT, kit.getState());
		
		assertTrue(controller.log.containsString("doAnim"));
		assertTrue(controller.log.containsString(kit.toString()));
		controller.log.clear();
		
		TestHelper.INSTANCE.callPrivateMethod("agents.ConveyorSystemAgent", "pickAndExecuteAnAction", conveyorSystemAgent);
		
		assertFalse(controller.log.containsString("doAnim"));
		assertFalse(controller.log.containsString(kit.toString()));
		
		conveyorSystemAgent.msgKitPickedUp(kit);
		
		TestHelper.INSTANCE.callPrivateMethod("agents.ConveyorSystemAgent", "pickAndExecuteAnAction", conveyorSystemAgent);
		assertEquals(KitState.MOVING_OUT, kit2.getState());
		
		assertTrue(controller.log.containsString("doAnim"));
		assertTrue(controller.log.containsString(kit2.toString()));
		controller.log.clear();
		
		TestHelper.INSTANCE.callPrivateMethod("agents.ConveyorSystemAgent", "pickAndExecuteAnAction", conveyorSystemAgent);
		
		assertFalse(controller.log.containsString("doAnim"));
		assertFalse(controller.log.containsString(kit.toString()));
		assertFalse(controller.log.containsString(kit2.toString()));
		assertEquals(1, conveyorSystemAgent.getEnteringKits().size());
		conveyorSystemAgent.msgKitPickedUp(kit2);
		
		assertEquals(0, conveyorSystemAgent.getEnteringKits().size());
		
		conveyorSystemAgent.msgKitDone(kit);
		
		assertEquals(1, conveyorSystemAgent.getExitingKits().size());
		
		TestHelper.INSTANCE.callPrivateMethod("agents.ConveyorSystemAgent", "pickAndExecuteAnAction", conveyorSystemAgent);
		
		assertTrue(controller.log.containsString("doAnim"));
		assertTrue(controller.log.containsString(kit.toString()));		
		assertEquals(kit, conveyorSystemAgent.getExitingKits().get(0));
		assertEquals(KitState.EXITING_CELL, kit.getState());
		
		conveyorSystemAgent.msgKitOutOfCell(null);
		
		assertEquals(0, conveyorSystemAgent.getExitingKits().size());
		assertEquals(0, conveyorSystemAgent.getEnteringKits().size());
		
		controller.log.clear();
		
		conveyorSystemAgent.msgKitDone(kit2);
		
		assertEquals(1, conveyorSystemAgent.getExitingKits().size());
		
		TestHelper.INSTANCE.callPrivateMethod("agents.ConveyorSystemAgent", "pickAndExecuteAnAction", conveyorSystemAgent);
		
		assertTrue(controller.log.containsString("doAnim"));
		assertTrue(controller.log.containsString(kit2.toString()));		
		assertEquals(kit2, conveyorSystemAgent.getExitingKits().get(0));
		assertEquals(KitState.EXITING_CELL, kit2.getState());
		
		conveyorSystemAgent.msgKitOutOfCell(null);
		
		assertEquals(0, conveyorSystemAgent.getExitingKits().size());
		assertEquals(0, conveyorSystemAgent.getEnteringKits().size());
		
	}
	
}
