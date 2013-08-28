package ml.core.vec;

import ml.core.math.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;

public class Vector3 {

	public double x;
	public double y;
	public double z;
	
	public static Vector3 fromForgeDir(ForgeDirection fd) {
		return new Vector3(fd.offsetX, fd.offsetY, fd.offsetZ);
	}
	
	public Vector3(double xin, double yin, double zin) {
		x = xin;
		y = yin;
		z = zin;
	}
	
	public Vector3(Vector3 c) {
		x=c.x;
		y=c.y;
		z=c.z;
	}

	public Vector3() {}
	
	public Vector3 copy() {
		return new Vector3(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vector3){
			Vector3 asV3 = (Vector3)obj;
			return asV3.x == x && asV3.y == y && asV3.z == z;
		}
		return false;
	}

	public double dotProd(Vector3 ov3) {
		return this.x*ov3.x + this.y*ov3.y + this.z*ov3.z;
	}

	public double magnitude() {
		return Math.sqrt(magSqr());
	}

	public double magSqr() {
		return Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
	}

	public Vector3 normalize() {
		double mag = magnitude();
		if (mag==0)
			return new Vector3(0, 0, 0);
		return divide(mag);
	}

	public Vector3 minus(Vector3 dif) {
		this.x -= dif.x;
		this.y -= dif.y;
		this.z -= dif.z;
		return this;
	}

	public Vector3 add(Vector3 a) {
		this.x += a.x;
		this.y += a.y;
		this.z += a.z;
		return this;
	}

	public Vector3 divide(double div) {
		this.x /= div;
		this.y /= div;
		this.z /= div;
		return this;
	}

	public Vector3 mult(double fac) {
		this.x *= fac;
		this.y *= fac;
		this.z *= fac;
		return this;
	}
	
	public Vector3 mult(Vector3 a) {
		this.x *= a.x;
		this.y *= a.y;
		this.z *= a.z;
		return this;
	}
	
	public Vector3 negate() {
		this.x = -x;
		this.y = -y;
		this.z = -z;
		return this;
	}

	/**
	 * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 */
	public Vector3 getInterceptOfYZ(Vector3 end, double tX) {
		double delX = end.x - x;
		double delY = end.y - y;
		double delZ = end.z - z;

		if (delX == 0) return null;
		double d = (tX - x) / delX;

		if (MathHelper.between(-1E-5, delX, 1E-5))
			return this;

		return new Vector3(tX, this.y + delY * d, this.z + delZ * d);
	}

	/**
	 * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 */
	public Vector3 getInterceptOfXZ(Vector3 end, double tY) {
		double delX = end.x - x;
		double delY = end.y - y;
		double delZ = end.z - z;

		if (delY == 0) return null;
		double d = (tY - y) / delY;

		if (MathHelper.between(-1E-5, delY, 1E-5))
			return this;

		return new Vector3(this.x + delX * d, tY, this.z + delZ * d);
	}

	/**
	 * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 */
	public Vector3 getInterceptOfXY(Vector3 end, double tZ) {
		double delX = end.x - x;
		double delY = end.y - y;
		double delZ = end.z - z;

		if (delZ == 0) return null;
		double d = (tZ - z) / delZ;

		if (MathHelper.between(-1E-5, delZ, 1E-5))
			return this;

		return new Vector3(this.x + delX * d, this.y + delY * d, tZ);
	}

	public Vec3 toVec3() {
		return Vec3.createVectorHelper(x, y, z);
	}

}