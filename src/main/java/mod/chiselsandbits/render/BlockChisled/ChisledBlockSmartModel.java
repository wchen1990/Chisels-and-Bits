
package mod.chiselsandbits.render.BlockChisled;

import java.util.WeakHashMap;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobState;
import mod.chiselsandbits.chiseledblock.data.VoxelNeighborRenderTracker;
import mod.chiselsandbits.render.BaseSmartModel;
import mod.chiselsandbits.render.MergedBakedModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ChisledBlockSmartModel extends BaseSmartModel implements ISmartItemModel, ISmartBlockModel
{
	@SuppressWarnings( "unchecked" )
	static private final WeakHashMap<ModelRenderState, ChisledBlockBaked>[] modelCache = new WeakHashMap[4];
	static private final WeakHashMap<VoxelBlobState, ChisledBlockBaked> solidCache = new WeakHashMap<VoxelBlobState, ChisledBlockBaked>();
	static private final WeakHashMap<ItemStack, IBakedModel> itemToModel = new WeakHashMap<ItemStack, IBakedModel>();

	static
	{
		final int count = EnumWorldBlockLayer.values().length;

		if ( modelCache.length != count )
		{
			throw new RuntimeException( "Invalid Number of EnumWorldBlockLayer" );
		}

		// setup layers.
		for ( final EnumWorldBlockLayer l : EnumWorldBlockLayer.values() )
		{
			modelCache[l.ordinal()] = new WeakHashMap<ModelRenderState, ChisledBlockBaked>();
		}
	}

	public static int getSides(
			final TileEntityBlockChiseled te )
	{
		final ChisledBlockBaked model = getCachedModel( te, EnumWorldBlockLayer.SOLID );
		return model.sides;
	}

	public static ChisledBlockBaked getCachedModel(
			final TileEntityBlockChiseled te,
			final EnumWorldBlockLayer layer )
	{
		final IExtendedBlockState myState = te.getBasicState();

		final VoxelBlobState data = myState.getValue( BlockChiseled.v_prop );
		final VoxelNeighborRenderTracker rTracker = myState.getValue( BlockChiseled.n_prop );
		Integer blockP = myState.getValue( BlockChiseled.block_prop );

		blockP = blockP == null ? 0 : blockP;

		return getCachedModel( blockP, data, getRenderState( rTracker, data ), layer, ChisledBlockBaked.CNB );
	}

	private static ChisledBlockBaked getCachedModel(
			final Integer blockP,
			final VoxelBlobState data,
			final ModelRenderState mrs,
			final EnumWorldBlockLayer layer,
			final VertexFormat format )
	{
		if ( data == null )
		{
			return new ChisledBlockBaked( blockP, layer, data, new ModelRenderState( null ), format );
		}

		ChisledBlockBaked out = null;

		if ( format == ChisledBlockBaked.CNB )
		{
			if ( layer == EnumWorldBlockLayer.SOLID )
			{
				out = solidCache.get( data );
			}
			else
			{
				out = mrs == null ? null : modelCache[layer.ordinal()].get( mrs );
			}
		}

		if ( out == null )
		{
			out = new ChisledBlockBaked( blockP, layer, data, mrs, format );

			if ( out.isEmpty() )
			{
				out = out.getEmptyModel();
			}

			if ( format == ChisledBlockBaked.CNB )
			{
				if ( layer == EnumWorldBlockLayer.SOLID )
				{
					solidCache.put( data, out );
				}
				else if ( mrs != null )
				{
					modelCache[layer.ordinal()].put( mrs, out );
				}
			}
		}

		return out;

	}

	@Override
	public IBakedModel handleBlockState(
			final IBlockState state )
	{
		final IExtendedBlockState myState = (IExtendedBlockState) state;

		final VoxelBlobState data = myState.getValue( BlockChiseled.v_prop );
		final VoxelNeighborRenderTracker rTracker = myState.getValue( BlockChiseled.n_prop );
		Integer blockP = myState.getValue( BlockChiseled.block_prop );

		blockP = blockP == null ? 0 : blockP;

		final EnumWorldBlockLayer layer = net.minecraftforge.client.MinecraftForgeClient.getRenderLayer();
		return getCachedModel( blockP, data, getRenderState( rTracker, data ), layer, ChisledBlockBaked.CNB );
	}

	private static ModelRenderState getRenderState(
			final VoxelNeighborRenderTracker renderTracker,
			final VoxelBlobState data )
	{
		if ( renderTracker != null )
		{
			return renderTracker.getRenderState( data );
		}

		return null;
	}

	@Override
	public IBakedModel handleItemState(
			final ItemStack stack )
	{
		IBakedModel mdl;
		mdl = itemToModel.get( stack );

		if ( mdl != null )
		{
			return mdl;
		}

		NBTTagCompound c = stack.getTagCompound();
		if ( c == null )
		{
			return this;
		}

		c = c.getCompoundTag( "BlockEntityTag" );
		if ( c == null )
		{
			return this;
		}

		final byte[] data = c.getByteArray( "v" );
		final Integer blockP = c.getInteger( "b" );

		final IFlexibleBakedModel[] models = new IFlexibleBakedModel[EnumWorldBlockLayer.values().length];
		for ( final EnumWorldBlockLayer l : EnumWorldBlockLayer.values() )
		{
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer( l );
			models[l.ordinal()] = getCachedModel( blockP, new VoxelBlobState( data, 0L ), null, l, DefaultVertexFormats.ITEM );
		}

		net.minecraftforge.client.ForgeHooksClient.setRenderLayer( EnumWorldBlockLayer.SOLID );

		mdl = new MergedBakedModel( models );

		itemToModel.put( stack, mdl );

		return mdl;
	}

}
