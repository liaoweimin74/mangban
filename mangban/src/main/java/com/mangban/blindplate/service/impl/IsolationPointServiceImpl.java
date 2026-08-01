package com.mangban.blindplate.service.impl;

import com.mangban.common.constant.GlobalConstant;
import com.mangban.common.domain.PageResult;
import com.mangban.common.exception.BusinessException;
import com.mangban.blindplate.domain.dto.*;
import com.mangban.blindplate.domain.entity.IsolationPoint;
import com.mangban.blindplate.domain.entity.Location;
import com.mangban.blindplate.domain.vo.IsolationPointVO;
import com.mangban.blindplate.repository.IsolationPointRepository;
import com.mangban.blindplate.repository.LocationRepository;
import com.mangban.blindplate.service.IsolationPointService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IsolationPointServiceImpl implements IsolationPointService {
    private final IsolationPointRepository isolationPointRepository;
    private final LocationRepository locationRepository;

    public IsolationPointServiceImpl(IsolationPointRepository isolationPointRepository,
                                     LocationRepository locationRepository) {
        this.isolationPointRepository = isolationPointRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public PageResult<IsolationPointVO> list(Long unitId, Long plantId, String code, String name,
                                              String medium, String hazardLevel, String status,
                                              String occupyStatus, int page, int size) {
        Specification<IsolationPoint> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (unitId != null) predicates.add(cb.equal(root.get("unitId"), unitId));
            if (plantId != null) {
                List<Long> unitIds = findUnitsByPlant(plantId).stream().map(Location::getId).toList();
                if (!unitIds.isEmpty()) {
                    predicates.add(root.get("unitId").in(unitIds));
                }
            }
            if (StringUtils.hasText(code)) predicates.add(cb.like(root.get("code"), "%" + code + "%"));
            if (StringUtils.hasText(name)) predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            if (StringUtils.hasText(medium)) predicates.add(cb.equal(root.get("medium"), medium));
            if (StringUtils.hasText(hazardLevel)) predicates.add(cb.equal(root.get("hazardLevel"), hazardLevel));
            if (StringUtils.hasText(status)) predicates.add(cb.equal(root.get("status"), status));
            if (StringUtils.hasText(occupyStatus)) predicates.add(cb.equal(root.get("occupyStatus"), occupyStatus));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<IsolationPoint> p = isolationPointRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id")));
        return new PageResult<>(p.getTotalElements(), page, size,
                p.getContent().stream().map(this::toVO).toList());
    }

    private List<Location> findUnitsByPlant(Long plantId) {
        List<Location> units = new ArrayList<>();
        List<Location> children = locationRepository.findByParentIdOrderBySortOrder(plantId);
        for (Location child : children) {
            if ("UNIT".equals(child.getType()) && child.getIsDeleted() == 0) {
                units.add(child);
            }
        }
        return units;
    }

    private IsolationPointVO toVO(IsolationPoint ip) {
        String unitName = null, plantName = null, factoryName = null;
        Optional<Location> unitOpt = locationRepository.findById(ip.getUnitId());
        if (unitOpt.isPresent()) {
            Location unit = unitOpt.get();
            unitName = unit.getName();
            if (unit.getParentId() != null) {
                Optional<Location> plantOpt = locationRepository.findById(unit.getParentId());
                if (plantOpt.isPresent()) {
                    Location plant = plantOpt.get();
                    plantName = plant.getName();
                    if (plant.getParentId() != null) {
                        Optional<Location> factoryOpt = locationRepository.findById(plant.getParentId());
                        if (factoryOpt.isPresent()) {
                            factoryName = factoryOpt.get().getName();
                        }
                    }
                }
            }
        }
        return new IsolationPointVO(ip.getId(), ip.getUnitId(), unitName, plantName, factoryName,
                ip.getCode(), ip.getName(), ip.getMedium(), ip.getPressureRating(),
                ip.getTemperatureRating(), ip.getHazardLevel(), ip.getPointType(),
                ip.getBlindSpec(), ip.getEquipmentTag(), ip.getPipelineNo(),
                ip.getStatus(), ip.getOccupyStatus(), ip.getRemark(),
                ip.getCreatedAt(), ip.getUpdatedAt());
    }

    @Override
    public IsolationPointVO getById(Long id) {
        IsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        return toVO(ip);
    }

    @Override
    @Transactional
    public IsolationPointVO create(IsolationPointCreateRequest request) {
        Location unit = locationRepository.findById(request.unitId())
                .orElseThrow(() -> new BusinessException("所属单元不存在"));
        if (!"UNIT".equals(unit.getType())) {
            throw new BusinessException("隔离点必须挂在单元下");
        }
        if (isolationPointRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException("编码已存在");
        }
        IsolationPoint ip = new IsolationPoint();
        ip.setUnitId(request.unitId());
        ip.setCode(request.code());
        ip.setName(request.name());
        ip.setMedium(request.medium());
        ip.setPressureRating(request.pressureRating());
        ip.setTemperatureRating(request.temperatureRating());
        ip.setHazardLevel(request.hazardLevel());
        ip.setPointType(request.pointType());
        ip.setBlindSpec(request.blindSpec());
        ip.setEquipmentTag(request.equipmentTag());
        ip.setPipelineNo(request.pipelineNo());
        ip.setRemark(request.remark());
        ip = isolationPointRepository.save(ip);
        return toVO(ip);
    }

    @Override
    @Transactional
    public IsolationPointVO update(Long id, IsolationPointUpdateRequest request) {
        IsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        if (request.unitId() != null) {
            Location unit = locationRepository.findById(request.unitId())
                    .orElseThrow(() -> new BusinessException("所属单元不存在"));
            if (!"UNIT".equals(unit.getType())) {
                throw new BusinessException("隔离点必须挂在单元下");
            }
            ip.setUnitId(request.unitId());
        }
        if (StringUtils.hasText(request.code())) {
            if (!request.code().equals(ip.getCode())) {
                if (isolationPointRepository.findByCode(request.code()).isPresent()) {
                    throw new BusinessException("编码已存在");
                }
            }
            ip.setCode(request.code());
        }
        if (StringUtils.hasText(request.name())) ip.setName(request.name());
        if (StringUtils.hasText(request.medium())) ip.setMedium(request.medium());
        if (StringUtils.hasText(request.pressureRating())) ip.setPressureRating(request.pressureRating());
        if (StringUtils.hasText(request.temperatureRating())) ip.setTemperatureRating(request.temperatureRating());
        if (StringUtils.hasText(request.hazardLevel())) ip.setHazardLevel(request.hazardLevel());
        if (StringUtils.hasText(request.pointType())) ip.setPointType(request.pointType());
        if (StringUtils.hasText(request.blindSpec())) ip.setBlindSpec(request.blindSpec());
        if (StringUtils.hasText(request.equipmentTag())) ip.setEquipmentTag(request.equipmentTag());
        if (StringUtils.hasText(request.pipelineNo())) ip.setPipelineNo(request.pipelineNo());
        if (StringUtils.hasText(request.remark())) ip.setRemark(request.remark());
        ip = isolationPointRepository.save(ip);
        return toVO(ip);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        IsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        ip.setIsDeleted(GlobalConstant.DELETED_YES);
        isolationPointRepository.save(ip);
    }

    @Override
    @Transactional
    public IsolationPointVO updateStatus(Long id, IsolationPointStatusRequest request) {
        if (!List.of("OPEN", "BLIND").contains(request.status())) {
            throw new BusinessException("无效状态值，仅支持 OPEN 或 BLIND");
        }
        IsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        ip.setStatus(request.status());
        ip = isolationPointRepository.save(ip);
        return toVO(ip);
    }

    @Override
    @Transactional
    public IsolationPointVO updateOccupy(Long id, IsolationPointOccupyRequest request) {
        if (!List.of("OCCUPIED", "FREE").contains(request.occupyStatus())) {
            throw new BusinessException("无效占用状态值，仅支持 OCCUPIED 或 FREE");
        }
        IsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        ip.setOccupyStatus(request.occupyStatus());
        ip = isolationPointRepository.save(ip);
        return toVO(ip);
    }
}