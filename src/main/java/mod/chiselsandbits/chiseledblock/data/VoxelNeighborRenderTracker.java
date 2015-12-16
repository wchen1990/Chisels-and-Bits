package mod.chiselsandbits.chiseledblock.data;

import java.lang.ref.WeakReference;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.render.BlockChisled.ModelRenderState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class VoxelNeighborRenderTracker
{
	private WeakReference<VoxelBlobState> lastCenter;
	private ModelRenderState lrs = null;

	private final VoxelBlobState[] sides = new VoxelBlobState[6];

	public void update(
			final World worldObj,
			final BlockPos pos )
	{
		for ( final EnumFacing f : EnumFacing.VALUES )
		{
			final TileEntity te = worldObj.getTileEntity( pos.offset( f ) );
			if ( te instanceof TileEntityBlockChiseled )
			{
				update( f, ( (TileEntityBlockChiseled) te ).getBasicState().getValue( BlockChiseled.v_prop ) );
			}
			else
			{
				update( f, null );
			}
		}
	}

	private void update(
			final EnumFacing f,
			final VoxelBlobState value )
	{
		if ( sides[f.ordinal()] == value )
		{
			return;
		}

		synchronized ( this )
		{
			sides[f.ordinal()] = value;
			lrs = null;
		}
	}

	public ModelRenderState getRenderState(
			final VoxelBlobState data )
	{
		if ( lrs == null || lastCenter == null )
		{
			lrs = new ModelRenderState( sides );
			updateCenter( data );
		}
		else if ( lastCenter.get() != data )
		{
			updateCenter( data );
			lrs = new ModelRenderState( sides );
		}

		return lrs;
	}

	private void updateCenter(
			final VoxelBlobState data )
	{
		lastCenter = new WeakReference<VoxelBlobState>( data );
	}

}
