package ml.core.tile;

import cpw.mods.fml.common.network.PacketDispatcher;
import ml.core.network.PacketDescribeConnectable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityConnectable extends TileEntity {

	public ForgeDirection facing = ForgeDirection.NORTH;
	public ForgeDirection linkedDir = ForgeDirection.UNKNOWN;
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setInteger("facing", facing.ordinal());
		tag.setInteger("linked", linkedDir.ordinal());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		facing = ForgeDirection.getOrientation(tag.getInteger("facing"));
		linkedDir = ForgeDirection.getOrientation(tag.getInteger("linked"));
	}
	
	public TileEntityConnectable getMaster() {
		if (linkedDir.offsetX+linkedDir.offsetY+linkedDir.offsetZ > 0)
			return this;
		TileEntityConnectable tec = getConnected();
		if (tec != null) return tec;
		return this;
	}
	
	public TileEntityConnectable getConnected() {
		TileEntity te = worldObj.getBlockTileEntity(xCoord+linkedDir.offsetX, yCoord+linkedDir.offsetY, zCoord+linkedDir.offsetZ);
		if (te instanceof TileEntityConnectable && te.getClass()==this.getClass()) {
			return (TileEntityConnectable)te;
		}
		return null; 
	}
	
	@Override
	public Packet getDescriptionPacket() {
		PacketDescribeConnectable pdc = new PacketDescribeConnectable(this, "MLCore");
		return pdc.convertToPkt250();
	}
	
	private void sendPacket() {
		PacketDispatcher.sendPacketToAllInDimension(getDescriptionPacket(), worldObj.getWorldInfo().getDimension());
	}
	
	public boolean canConnectWith(TileEntityConnectable rtec) {
		return rtec.getClass() == this.getClass() && rtec.facing==this.facing;
	}
	
	private boolean tryConnection(ForgeDirection fd){
		TileEntity rte = worldObj.getBlockTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
		if (rte instanceof TileEntityConnectable){
			TileEntityConnectable rtec = (TileEntityConnectable)rte;
			if (rtec.facing == this.facing &&
					rtec.linkedDir == ForgeDirection.UNKNOWN &&
					canConnectWith(rtec)){
				
				linkedDir = fd;
				rtec.linkedDir = fd.getOpposite();
				rtec.sendPacket();
				return true;
			}
		}
		
		return false;
	}
	
	public void tryConnection(){
		if (!tryConnection(ForgeDirection.UP)) tryConnection(ForgeDirection.DOWN);
		sendPacket();
	}
	
	public void refreshConnection(){
		if (linkedDir != ForgeDirection.UNKNOWN) {
			TileEntity te = worldObj.getBlockTileEntity(xCoord+linkedDir.offsetX, yCoord+linkedDir.offsetY, zCoord+linkedDir.offsetZ);
			if (!(te instanceof TileEntityConnectable) || !canConnectWith((TileEntityConnectable)te) ||
					((TileEntityConnectable)te).linkedDir != linkedDir.getOpposite()){
				linkedDir = ForgeDirection.UNKNOWN;
				sendPacket();
			}
		}
	}
}