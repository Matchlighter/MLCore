package ml.core.geo;

import net.minecraftforge.common.ForgeDirection;

public class BlockCoord {
	public int x;
	public int y;
	public int z;
	
	public BlockCoord(int ix, int iy, int iz) {
		x = ix;
		y = iy;
		z = iz;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlockCoord){
			BlockCoord asV3 = (BlockCoord)obj;
			return asV3.x == x && asV3.y == y && asV3.z == z;
		}
		return false;
	}

	public BlockCoord set(int ix, int iy, int iz) {
		x = ix;
		y = iy;
		z = iz;
		return this;
	}
	
	public double magnitude(){
		return Math.sqrt(magSqr());
	}

	public int magSqr(){
		return x*x+y*y+z*z;
	}
	
	public BlockCoord translate(ForgeDirection fd) {
		return translate(fd, 1);
	}
	
	public BlockCoord translate(ForgeDirection fd, int i) {
		x+=fd.offsetX*i;
		y+=fd.offsetY*i;
		z+=fd.offsetZ*i;
		return this;
	}
}
