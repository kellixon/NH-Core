package com.nhave.nhc.blocks;

import java.util.List;

import com.nhave.nhc.api.blocks.IHudBlock;
import com.nhave.nhc.api.items.IToolStationHud;
import com.nhave.nhc.helpers.ItemHelper;
import com.nhave.nhc.helpers.TooltipHelper;
import com.nhave.nhc.registry.ModItems;
import com.nhave.nhc.tiles.TileEntityToolStation;
import com.nhave.nhc.util.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockToolStation extends BlockMachineBase implements IHudBlock
{
	public BlockToolStation(String name)
	{
		super(name, true);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState blockState)
	{
		return false;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState blockState)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState blockState)
	{
		return new TileEntityToolStation();
	}
	
	@Override
	public boolean doBlockRotation(World world, BlockPos pos, EnumFacing axis)
	{
		if (axis == EnumFacing.UP) return false;
		return super.doBlockRotation(world, pos, axis);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (hand == EnumHand.MAIN_HAND)
		{
			TileEntityToolStation tile = (TileEntityToolStation) worldIn.getTileEntity(pos);
			if (tile.hasOwner() && !tile.getOwner().equals(playerIn.getName())) return false;
			
			if (playerIn.isSneaking() && playerIn.getHeldItem(hand).getItem() == ModItems.itemLock && !tile.hasOwner())
			{
				tile.setOwner(playerIn.getName());
				playerIn.getHeldItem(hand).shrink(1);
				playerIn.swingArm(EnumHand.MAIN_HAND);
				return !worldIn.isRemote;
			}
			else if (playerIn.isSneaking() && playerIn.getHeldItem(hand).getItem() == ModItems.itemKey && tile.hasOwner())
			{
				tile.setOwner(null);
				ItemHelper.addItemToPlayer(playerIn, new ItemStack(ModItems.itemLock));
				playerIn.swingArm(EnumHand.MAIN_HAND);
				return !worldIn.isRemote;
			}
			else if (facing == EnumFacing.UP && tile != null && !playerIn.isSneaking() && worldIn.isAirBlock(pos.up(1)))
			{
				playerIn.swingArm(hand);
				return tile.onTileActivated(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn);
			}
			/*else if (!playerIn.getHeldItemMainhand().isEmpty() && ItemHelper.isToolWrench(playerIn, playerIn.getHeldItemMainhand(), pos.getX(), pos.getY(), pos.getZ()))
			{
				if (playerIn.isSneaking())
				{
					if (!worldIn.isRemote)
					{
						ItemHelper.dismantleBlock(worldIn, pos, state, playerIn);
						ItemHelper.useWrench(playerIn, playerIn.getHeldItemMainhand(), pos.getX(), pos.getY(), pos.getZ());
						return true;
					}
					else
					{
						playerIn.playSound(this.blockSoundType.getPlaceSound(), 1.0F, 0.6F);
						playerIn.swingArm(EnumHand.MAIN_HAND);
					}
				}
				else
				{
					if (!worldIn.isRemote)
					{
						this.rotateBlock(worldIn, pos, facing);
						ItemHelper.useWrench(playerIn, playerIn.getHeldItemMainhand(), pos.getX(), pos.getY(), pos.getZ());
						return true;
					}
					else
					{
						playerIn.playSound(this.blockSoundType.getPlaceSound(), 1.0F, 0.6F);
						playerIn.swingArm(EnumHand.MAIN_HAND);
					}
				}
			}*/
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		if (!worldIn.isRemote)
        {
    		if (worldIn.getTileEntity(pos) != null && !worldIn.isAirBlock(pos.up(1)))
    		{
    			TileEntityToolStation tile = (TileEntityToolStation) worldIn.getTileEntity(pos);
    			ItemStack stack = tile.getItemStack();
    			if (stack != null) ItemHelper.dropBlockAsItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
    			tile.clearItemStack();
    		}
        }
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player)
	{
		if (!world.isRemote)
        {
    		if (world.getTileEntity(blockPos) != null)
    		{
    			TileEntityToolStation tile = (TileEntityToolStation) world.getTileEntity(blockPos);
    			if (tile.hasOwner()) ItemHelper.dropBlockAsItem(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ModItems.itemLock));
    			ItemStack stack = tile.getItemStack();
    			if (stack != null) ItemHelper.dropBlockAsItem(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), stack);
    		}
        }
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
	{
		if (StringUtils.isShiftKeyDown()) TooltipHelper.addSplitString(tooltip, StringUtils.localize("tooltip.nhc.toolstation"), ";", StringUtils.GRAY);
		else tooltip.add(StringUtils.shiftForInfo);
	}
	
	@Override
	public void addHudInfo(World world, BlockPos pos, IBlockState state, List list)
	{
		TileEntityToolStation tile = (TileEntityToolStation) world.getTileEntity(pos);
		if (tile != null)
		{
			list.add(StringUtils.format(getLocalizedName(), StringUtils.YELLOW, StringUtils.ITALIC));
			ItemStack stack = tile.getItemStack();
			if (stack != null && !stack.isEmpty())
			{
				list.add(StringUtils.localize("tooltip.nhc.toolstation.item") + ": " + StringUtils.format(StringUtils.limitString(stack.getDisplayName(), 20), StringUtils.YELLOW, StringUtils.ITALIC));
				if (stack.getItem() instanceof IToolStationHud) ((IToolStationHud) stack.getItem()).addToolStationInfo(stack, list);
			}
		}
	}
}