package com.example.inventory.mapper;
import com.example.inventory.model.BuyIn; import java.util.List;
public interface BuyInMapper { List<BuyIn> findAll(); int insert(BuyIn buyIn); String maxCodeLike(String prefix); }
