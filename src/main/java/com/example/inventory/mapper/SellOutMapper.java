package com.example.inventory.mapper;
import com.example.inventory.model.SellOut; import java.util.List;
public interface SellOutMapper { List<SellOut> findAll(); int insert(SellOut sellOut); String maxCodeLike(String prefix); }
